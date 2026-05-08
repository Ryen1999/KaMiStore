package com.shop.kami.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;

import java.nio.charset.StandardCharsets;

/**
 * 卡密加解密工具类
 * <p>
 * 提供卡密内容的AES加密、解密功能和SHA256哈希功能。
 * AES加密用于安全存储卡密明文，SHA256哈希用于导入时去重校验。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
public final class KamiEncryptUtil {

    /** 默认AES加密密钥（后续改为从配置读取） */
    private static final String DEFAULT_KEY = "shop-kami-secret-key-2024";

    /**
     * 私有构造方法，防止实例化
     */
    private KamiEncryptUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * AES加密卡密内容
     *
     * @param content 卡密明文内容
     * @param key     加密密钥
     * @return 加密后的十六进制字符串
     */
    public static String encrypt(String content, String key) {
        // 使用MD5将密钥转换为16字节的AES密钥
        byte[] keyBytes = SecureUtil.md5(key).substring(0, 16).getBytes(StandardCharsets.UTF_8);
        AES aes = SecureUtil.aes(keyBytes);
        return aes.encryptHex(content);
    }

    /**
     * AES解密卡密内容
     *
     * @param encrypted 加密后的十六进制字符串
     * @param key       解密密钥
     * @return 解密后的卡密明文
     */
    public static String decrypt(String encrypted, String key) {
        // 使用MD5将密钥转换为16字节的AES密钥
        byte[] keyBytes = SecureUtil.md5(key).substring(0, 16).getBytes(StandardCharsets.UTF_8);
        AES aes = SecureUtil.aes(keyBytes);
        return aes.decryptStr(encrypted);
    }

    /**
     * SHA256哈希（用于卡密去重校验）
     *
     * @param content 卡密明文内容
     * @return SHA256哈希值（十六进制字符串）
     */
    public static String hash(String content) {
        return SecureUtil.sha256(content);
    }

    /**
     * 使用默认密钥进行AES加密
     *
     * @param content 卡密明文内容
     * @return 加密后的十六进制字符串
     */
    public static String encrypt(String content) {
        return encrypt(content, DEFAULT_KEY);
    }

    /**
     * 使用默认密钥进行AES解密
     *
     * @param encrypted 加密后的十六进制字符串
     * @return 解密后的卡密明文
     */
    public static String decrypt(String encrypted) {
        return decrypt(encrypted, DEFAULT_KEY);
    }
}
