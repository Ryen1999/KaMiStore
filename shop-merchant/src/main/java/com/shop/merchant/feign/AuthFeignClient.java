package com.shop.merchant.feign;

import com.shop.common.core.result.R;
import com.shop.merchant.dto.ChangePasswordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "shop-auth", path = "/auth")
public interface AuthFeignClient {

    @PutMapping("/password")
    R<Void> changePassword(@RequestBody ChangePasswordDTO dto);
}
