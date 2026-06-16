package com.ruoyi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 对象存储配置
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties
{
    /** 是否启用 MinIO 上传 */
    private boolean enabled = true;

    /** 服务地址，如 http://127.0.0.1:9000 */
    private String endpoint = "http://127.0.0.1:9000";

    /** 访问密钥 */
    private String accessKey = "minioadmin";

    /** 访问密钥密码 */
    private String secretKey = "minioadmin";

    /** 默认桶名称 */
    private String bucketName = "yiyayi-erp";

    /** 对象名前缀 */
    private String objectPrefix = "erp";

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    public void setAccessKey(String accessKey)
    {
        this.accessKey = accessKey;
    }

    public String getSecretKey()
    {
        return secretKey;
    }

    public void setSecretKey(String secretKey)
    {
        this.secretKey = secretKey;
    }

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public String getObjectPrefix()
    {
        return objectPrefix;
    }

    public void setObjectPrefix(String objectPrefix)
    {
        this.objectPrefix = objectPrefix;
    }
}
