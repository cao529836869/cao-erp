package com.ruoyi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.minio.MinioClient;

/**
 * MinIO 客户端配置
 *
 * @author ruoyi
 */
@Configuration
public class MinioConfig
{
    @Bean
    public MinioClient minioClient(MinioProperties minioProperties)
    {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }
}
