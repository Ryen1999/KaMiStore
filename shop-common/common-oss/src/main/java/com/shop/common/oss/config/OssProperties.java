package com.shop.common.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * 对象存储配置属性
 * <p>
 * 从application.yml中读取oss相关配置。
 * 支持阿里云OSS和MinIO两种存储方式，通过type属性切换。
 * </p>
 *
 * @author shop
 * @since 1.0.0
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "shop.oss")
public class OssProperties {

    /** 存储类型：aliyun / minio */
    private String type = "aliyun";

    // ========== 阿里云OSS配置 ==========

    /** 阿里云OSS endpoint */
    private String endpoint;

    /** 阿里云OSS accessKeyId */
    private String accessKeyId;

    /** 阿里云OSS accessKeySecret */
    private String accessKeySecret;

    /** 阿里云OSS bucketName */
    private String bucketName;

    // ========== MinIO配置 ==========

    /** MinIO 服务地址 */
    private String minioEndpoint;

    /** MinIO accessKey */
    private String minioAccessKey;

    /** MinIO secretKey */
    private String minioSecretKey;

    /** MinIO bucket名称 */
    private String minioBucket;
}
