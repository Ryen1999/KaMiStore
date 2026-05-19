package com.shop.merchant.service;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.common.core.exception.BizException;
import com.shop.common.core.result.R;
import com.shop.merchant.dto.AlipayPrepayVO;
import com.shop.merchant.dto.PublicCreateOrderDTO;
import com.shop.merchant.dto.PublicOrderVO;
import com.shop.merchant.entity.Order;
import com.shop.merchant.entity.OrderKamiBinding;
import com.shop.merchant.entity.PaymentRecord;
import com.shop.merchant.entity.ProfitSharingRecord;
import com.shop.merchant.entity.SettlementConfig;
import com.shop.merchant.entity.StoreConfig;
import com.shop.merchant.feign.KamiFeignClient;
import com.shop.merchant.feign.ProductFeignClient;
import com.shop.merchant.feign.dto.KamiAllocateResultDTO;
import com.shop.merchant.feign.dto.ProductDetailDTO;
import com.shop.merchant.feign.dto.ProductSkuDTO;
import com.shop.merchant.mapper.OrderKamiBindingMapper;
import com.shop.merchant.mapper.OrderMapper;
import com.shop.merchant.mapper.PaymentRecordMapper;
import com.shop.merchant.mapper.ProfitSharingRecordMapper;
import com.shop.merchant.mapper.SettlementConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicOrderService {

    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.1000");
    private static final String KAMI_AES_KEY = "shop-kami-binding-encrypt-2026";
    private static final int ORDER_TYPE_KAMI = 2;

    private final StoreConfigService storeConfigService;
    private final ProductFeignClient productFeignClient;
    private final KamiFeignClient kamiFeignClient;
    private final AlipayService alipayService;
    private final OrderMapper orderMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final ProfitSharingRecordMapper profitSharingRecordMapper;
    private final SettlementConfigMapper settlementConfigMapper;
    private final OrderKamiBindingMapper orderKamiBindingMapper;

    @Value("${shop.internal-token:shop-internal}")
    private String internalToken;

    @Value("${shop.kami.lock-timeout-seconds:900}")
    private int kamiLockTimeoutSeconds;

    @Transactional(rollbackFor = Exception.class)
    public PublicOrderVO createOrder(PublicCreateOrderDTO dto) {
        validateCreateRequest(dto);

        StoreConfig store = storeConfigService.getByShopCode(dto.getShopCode());
        if (store == null || Integer.valueOf(0).equals(store.getStoreEnabled())) {
            throw new BizException("store unavailable");
        }

        ProductDetailDTO product = fetchProduct(store.getTenantId(), dto.getProductId());
        if (!store.getTenantId().equals(product.getTenantId())) {
            throw new BizException("product tenant mismatch");
        }
        if (!Integer.valueOf(2).equals(product.getStatus())) {
            throw new BizException("product is not online");
        }

        ProductSkuDTO sku = chooseSku(product.getSkuList(), dto.getSkuId());
        int quantity = dto.getQuantity() == null ? 1 : dto.getQuantity();
        if (quantity <= 0 || quantity > 99) {
            throw new BizException("invalid quantity");
        }
        if (product.getTotalStock() == null || product.getTotalStock() < quantity) {
            throw new BizException("insufficient stock");
        }

        BigDecimal commissionRate = getCommissionRate(store.getTenantId());
        BigDecimal totalAmount = sku.getPrice().multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal platformAmount = totalAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal merchantAmount = totalAmount.subtract(platformAmount).setScale(2, RoundingMode.HALF_UP);
        String orderNo = generateOrderNo();

        List<Long> lockedKamiIds = new ArrayList<>();
        if (Integer.valueOf(ORDER_TYPE_KAMI).equals(product.getProductType())) {
            Map<String, Object> lockDTO = new HashMap<>(6);
            lockDTO.put("tenantId", store.getTenantId());
            lockDTO.put("productId", product.getId());
            lockDTO.put("skuId", sku.getId());
            lockDTO.put("quantity", quantity);
            lockDTO.put("orderNo", orderNo);
            lockDTO.put("lockTtlSeconds", kamiLockTimeoutSeconds);

            R<List<Long>> lockResult = kamiFeignClient.lockKami(internalToken, lockDTO);
            if (lockResult == null || !lockResult.isSuccess() || lockResult.getData() == null) {
                throw new BizException(lockResult == null ? "kami lock failed" : lockResult.getMsg());
            }
            lockedKamiIds = lockResult.getData();
            if (lockedKamiIds.size() < quantity) {
                throw new BizException("insufficient kami stock");
            }
        }

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setTenantId(store.getTenantId());
        order.setProductId(product.getId());
        order.setProductName(product.getProductName());
        order.setSkuId(sku.getId());
        order.setSkuName(sku.getSkuName());
        order.setQuantity(quantity);
        order.setOrderType(product.getProductType() == null ? ORDER_TYPE_KAMI : product.getProductType());
        order.setContactType(dto.getContactType() == null ? 1 : dto.getContactType());
        order.setContactValue(dto.getContactValue());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);
        order.setPlatformCommissionAmount(platformAmount);
        order.setMerchantSettleAmount(merchantAmount);
        order.setPayMethod(toPayMethod(dto.getPaymentMethod()));
        order.setStatus(0);
        order.setSettleStatus(0);
        orderMapper.insert(order);

        PaymentRecord payment = new PaymentRecord();
        payment.setTenantId(store.getTenantId());
        payment.setOrderNo(orderNo);
        payment.setChannel(normalizeChannel(dto.getPaymentMethod()));
        payment.setPayAmount(totalAmount);
        payment.setPayStatus(0);
        payment.setVerified(false);
        paymentRecordMapper.insert(payment);

        ProfitSharingRecord sharing = new ProfitSharingRecord();
        sharing.setTenantId(store.getTenantId());
        sharing.setOrderNo(orderNo);
        sharing.setOrderAmount(totalAmount);
        sharing.setCommissionRate(commissionRate);
        sharing.setPlatformAmount(platformAmount);
        sharing.setMerchantAmount(merchantAmount);
        sharing.setChannel(normalizeChannel(dto.getPaymentMethod()));
        sharing.setStatus(0);
        profitSharingRecordMapper.insert(sharing);

        log.info("public order created orderNo={}, productId={}, quantity={}, amount={}, lockedKami={}",
                orderNo, product.getId(), quantity, totalAmount, lockedKamiIds.size());

        PublicOrderVO vo = toPublicOrderVO(order);
        vo.setPayMessage("order created, waiting for payment");
        return vo;
    }

    public AlipayPrepayVO createAlipayPrepay(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            throw new BizException("order is not payable");
        }

        String subject = order.getProductName() != null ? order.getProductName() : "virtual product";
        if (order.getQuantity() != null && order.getQuantity() > 1) {
            subject += " x" + order.getQuantity();
        }

        AlipayPrepayVO prepayVO = alipayService.createPrePay(orderNo, order.getPayAmount(), subject);
        log.info("alipay prepay created orderNo={}, outTradeNo={}", orderNo, prepayVO.getOutTradeNo());
        return prepayVO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markPaidByCallback(String orderNo, String channelTradeNo, String notifyPayload) {
        Order order = getOrderByNo(orderNo);

        if (!Integer.valueOf(0).equals(order.getStatus())) {
            log.info("order already handled, skip callback orderNo={}, status={}", orderNo, order.getStatus());
            return;
        }

        if (!Integer.valueOf(ORDER_TYPE_KAMI).equals(order.getOrderType())) {
            R<Void> stockResult = productFeignClient.updateStock(
                    internalToken, order.getTenantId(), order.getProductId(), -order.getQuantity());
            if (stockResult == null || !stockResult.isSuccess()) {
                log.error("product stock deduction failed orderNo={}, tenantId={}, productId={}, quantity={}, result={}",
                        orderNo, order.getTenantId(), order.getProductId(), order.getQuantity(), stockResult);
                throw new BizException(stockResult == null ? "product stock deduction failed" : stockResult.getMsg());
            }
        }

        List<String> kamiPlainContents = new ArrayList<>();
        if (Integer.valueOf(ORDER_TYPE_KAMI).equals(order.getOrderType())) {
            Map<String, Object> confirmDTO = new HashMap<>(3);
            confirmDTO.put("tenantId", order.getTenantId());
            confirmDTO.put("orderNo", orderNo);

            R<List<KamiAllocateResultDTO>> confirmResult = kamiFeignClient.confirmKami(internalToken, confirmDTO);
            if (confirmResult == null || !confirmResult.isSuccess() || confirmResult.getData() == null) {
                log.error("kami confirm failed orderNo={}, tenantId={}, result={}", orderNo, order.getTenantId(), confirmResult);
                throw new BizException(confirmResult == null ? "kami allocation failed" : confirmResult.getMsg());
            }

            for (KamiAllocateResultDTO item : confirmResult.getData()) {
                kamiPlainContents.add(item.getPlainContent());
                OrderKamiBinding binding = new OrderKamiBinding();
                binding.setTenantId(order.getTenantId());
                binding.setOrderNo(orderNo);
                binding.setKamiItemId(item.getKamiItemId());
                binding.setKamiContentEncrypted(encryptKamiContent(item.getPlainContent()));
                binding.setDeliveredAt(LocalDateTime.now());
                orderKamiBindingMapper.insert(binding);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        order.setStatus(1);
        order.setPayTradeNo(channelTradeNo);
        order.setPayTime(now);
        order.setDeliverTime(now);
        order.setSettleStatus(0);
        orderMapper.updateById(order);

        updatePaymentRecord(orderNo, channelTradeNo, notifyPayload, now);
        updateProfitSharingRecord(orderNo);

        log.info("payment callback handled orderNo={}, tradeNo={}, kamiCount={}",
                orderNo, channelTradeNo, kamiPlainContents.size());
    }

    @Transactional(rollbackFor = Exception.class)
    public PublicOrderVO syncAlipayPayment(String orderNo) {
        Order order = getOrderByNo(orderNo);
        if (Integer.valueOf(0).equals(order.getStatus())) {
            Map<String, String> paidTrade = alipayService.queryPaidTrade(orderNo);
            if (paidTrade != null) {
                String tradeNo = paidTrade.get("trade_no");
                markPaidByCallback(orderNo, tradeNo, JSONUtil.toJsonStr(paidTrade));
            }
        }
        return toPublicOrderVO(getOrderByNo(orderNo));
    }

    public PublicOrderVO queryOrder(String orderNo, String contactValue) {
        Order order = getOrderByNo(orderNo);
        PublicOrderVO vo = toPublicOrderVO(order);

        if (order.getStatus() != null && order.getStatus() >= 1) {
            if (!StringUtils.hasText(contactValue)) {
                vo.setPayMessage("contact value is required to view kami");
                return vo;
            }
            if (!contactValue.equals(order.getContactValue())) {
                vo.setPayMessage("contact value mismatch");
                return vo;
            }

            List<OrderKamiBinding> bindings = orderKamiBindingMapper.selectList(
                    new LambdaQueryWrapper<OrderKamiBinding>().eq(OrderKamiBinding::getOrderNo, orderNo));
            List<String> kamiList = bindings.stream()
                    .map(b -> decryptKamiContent(b.getKamiContentEncrypted()))
                    .collect(Collectors.toList());
            vo.setKamiList(kamiList);
            vo.setPayMessage("payment success");
        }

        return vo;
    }

    private BigDecimal getCommissionRate(Long tenantId) {
        SettlementConfig config = settlementConfigMapper.selectOne(
                new LambdaQueryWrapper<SettlementConfig>().eq(SettlementConfig::getTenantId, tenantId));
        if (config != null && config.getPlatformCommissionRate() != null
                && config.getPlatformCommissionRate().compareTo(BigDecimal.ZERO) > 0) {
            return config.getPlatformCommissionRate();
        }
        return DEFAULT_COMMISSION_RATE;
    }

    private void updatePaymentRecord(String orderNo, String channelTradeNo, String notifyPayload, LocalDateTime now) {
        PaymentRecord payment = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>().eq(PaymentRecord::getOrderNo, orderNo));
        if (payment != null) {
            payment.setChannelTradeNo(channelTradeNo);
            payment.setPayStatus(1);
            payment.setVerified(true);
            payment.setNotifyPayload(notifyPayload);
            payment.setNotifyTime(now);
            paymentRecordMapper.updateById(payment);
        }
    }

    private void updateProfitSharingRecord(String orderNo) {
        ProfitSharingRecord sharing = profitSharingRecordMapper.selectOne(
                new LambdaQueryWrapper<ProfitSharingRecord>().eq(ProfitSharingRecord::getOrderNo, orderNo));
        if (sharing != null) {
            sharing.setStatus(1);
            profitSharingRecordMapper.updateById(sharing);
        }
    }

    private String encryptKamiContent(String plainContent) {
        byte[] keyBytes = SecureUtil.md5(KAMI_AES_KEY).substring(0, 16).getBytes(StandardCharsets.UTF_8);
        AES aes = SecureUtil.aes(keyBytes);
        return aes.encryptHex(plainContent);
    }

    private String decryptKamiContent(String encrypted) {
        byte[] keyBytes = SecureUtil.md5(KAMI_AES_KEY).substring(0, 16).getBytes(StandardCharsets.UTF_8);
        AES aes = SecureUtil.aes(keyBytes);
        return aes.decryptStr(encrypted);
    }

    private ProductDetailDTO fetchProduct(Long tenantId, Long productId) {
        R<ProductDetailDTO> result = productFeignClient.getProductDetail(tenantId, productId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BizException(result == null ? "product service unavailable" : result.getMsg());
        }
        return result.getData();
    }

    private ProductSkuDTO chooseSku(List<ProductSkuDTO> skuList, Long skuId) {
        if (skuList == null || skuList.isEmpty()) {
            throw new BizException("sku not configured");
        }
        if (skuId != null) {
            return skuList.stream()
                    .filter(item -> skuId.equals(item.getId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException("sku not found"));
        }
        return skuList.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getStatus()))
                .min(Comparator.comparing(ProductSkuDTO::getPrice))
                .orElseThrow(() -> new BizException("no available sku"));
    }

    private Order getOrderByNo(String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException("order not found");
        }
        return order;
    }

    private void validateCreateRequest(PublicCreateOrderDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getShopCode())) {
            throw new BizException("shop code is required");
        }
        if (dto.getProductId() == null) {
            throw new BizException("product is required");
        }
        if (!StringUtils.hasText(dto.getContactValue())) {
            throw new BizException("contact value is required");
        }
        if (!StringUtils.hasText(dto.getPaymentMethod())) {
            throw new BizException("payment method is required");
        }
    }

    private String generateOrderNo() {
        return "OD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private Integer toPayMethod(String paymentMethod) {
        String channel = normalizeChannel(paymentMethod);
        if ("wechat".equals(channel)) {
            return 1;
        }
        if ("alipay".equals(channel)) {
            return 2;
        }
        return 9;
    }

    private String normalizeChannel(String paymentMethod) {
        if (paymentMethod == null) {
            return "unknown";
        }
        if (paymentMethod.contains("wechat")) {
            return "wechat";
        }
        if (paymentMethod.contains("alipay")) {
            return "alipay";
        }
        return paymentMethod;
    }

    private PublicOrderVO toPublicOrderVO(Order order) {
        PublicOrderVO vo = new PublicOrderVO();
        vo.setOrderNo(order.getOrderNo());
        StoreConfig store = storeConfigService.getByTenantId(order.getTenantId());
        if (store != null) {
            vo.setShopCode(store.getShopCode());
        }
        vo.setStatus(order.getStatus());
        vo.setProductName(order.getProductName());
        vo.setSkuName(order.getSkuName());
        vo.setQuantity(order.getQuantity());
        vo.setAmount(order.getPayAmount());
        vo.setPaymentMethod(order.getPayMethod() == null ? null : order.getPayMethod().toString());
        return vo;
    }
}
