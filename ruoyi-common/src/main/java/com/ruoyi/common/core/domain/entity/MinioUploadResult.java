package com.ruoyi.common.core.domain.entity;

/**
 * MinIO 上传结果
 *
 * @author ruoyi
 */
public class MinioUploadResult
{
    /** 桶名称 */
    private String bucketName;

    /** 对象名称 */
    private String objectName;

    /** 访问地址 */
    private String url;

    /** 原始文件名 */
    private String originalFilename;

    /** 新文件名 */
    private String newFileName;

    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public String getObjectName()
    {
        return objectName;
    }

    public void setObjectName(String objectName)
    {
        this.objectName = objectName;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getOriginalFilename()
    {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename)
    {
        this.originalFilename = originalFilename;
    }

    public String getNewFileName()
    {
        return newFileName;
    }

    public void setNewFileName(String newFileName)
    {
        this.newFileName = newFileName;
    }
}
