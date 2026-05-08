package com.shop.common.oss.service;

import com.shop.common.oss.config.OssProperties;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 对象存储服务
 *
 * @author shop
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {

    private final OssProperties ossProperties;

    private MinioClient minioClient;
    private OSS aliyunOssClient;

    @PostConstruct
    public void init() {
        String type = ossProperties.getType();
        
        if ("minio".equals(type)) {
            if (ossProperties.getMinioEndpoint() == null || ossProperties.getMinioEndpoint().isEmpty()) {
                log.warn("MinIO配置为空，客户端未初始化");
                return;
            }
            this.minioClient = MinioClient.builder()
                    .endpoint(ossProperties.getMinioEndpoint())
                    .credentials(ossProperties.getMinioAccessKey(), ossProperties.getMinioSecretKey())
                    .build();
            log.info("MinIO客户端初始化完成");
            
        } else if ("aliyun".equals(type)) {
            if (ossProperties.getEndpoint() == null || ossProperties.getEndpoint().isEmpty()) {
                log.warn("阿里云OSS配置为空，客户端未初始化");
                return;
            }
            this.aliyunOssClient = new OSSClientBuilder().build(
                    ossProperties.getEndpoint(),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );
            log.info("阿里云OSS客户端初始化完成");
        }
    }

    /**
     * 上传文件，返回永久有效的访问URL
     */
    public String upload(MultipartFile file, String dir) {
        if (minioClient == null && aliyunOssClient == null) {
            init();
        }
            
        if (minioClient == null && aliyunOssClient == null) {
            throw new RuntimeException("OSS服务未配置");
        }
            
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String objectName = dir + "/" + date + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
    
        try {
            String type = ossProperties.getType();
            
            if ("aliyun".equals(type) && aliyunOssClient != null) {
                aliyunOssClient.putObject(
                        ossProperties.getBucketName(),
                        objectName,
                        file.getInputStream()
                );
                // 返回永久有效的URL
                String url = "https://" + ossProperties.getBucketName() + "." + 
                            ossProperties.getEndpoint() + "/" + objectName;
                log.info("阿里云OSS上传成功: {}", url);
                return url;
                
            } else if ("minio".equals(type) && minioClient != null) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(ossProperties.getMinioBucket())
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build());
                // 返回永久有效的URL
                String url = ossProperties.getMinioEndpoint() + "/" + 
                            ossProperties.getMinioBucket() + "/" + objectName;
                log.info("MinIO上传成功: {}", url);
                return url;
            }
            
            throw new RuntimeException("OSS客户端未正确初始化");
            
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
}