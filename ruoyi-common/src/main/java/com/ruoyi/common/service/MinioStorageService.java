package com.ruoyi.common.service;

import java.io.InputStream;
import java.time.LocalDate;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.MinioProperties;
import com.ruoyi.common.core.domain.entity.MinioUploadResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;

/**
 * MinIO 对象存储服务
 *
 * @author ruoyi
 */
@Service
public class MinioStorageService
{
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProperties;

    /**
     * 上传文件到 MinIO
     *
     * @param file 文件
     * @param folder 业务目录
     * @param allowedExtension 允许的文件后缀
     * @return 上传结果
     */
    public MinioUploadResult upload(MultipartFile file, String folder, String[] allowedExtension) throws Exception
    {
        if (!minioProperties.isEnabled())
        {
            throw new ServiceException("MinIO 上传未启用");
        }
        FileUploadUtils.assertAllowed(file, allowedExtension);
        ensureBucketExists();

        String objectName = buildObjectName(file, folder);
        try (InputStream inputStream = file.getInputStream())
        {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(getContentType(file))
                    .build());
        }

        MinioUploadResult result = new MinioUploadResult();
        result.setBucketName(minioProperties.getBucketName());
        result.setObjectName(objectName);
        result.setUrl("/common/minio/preview/" + objectName);
        result.setOriginalFilename(file.getOriginalFilename());
        result.setNewFileName(FilenameUtils.getName(objectName));
        return result;
    }

    /**
     * 获取对象输入流
     *
     * @param objectName 对象名称
     * @return 对象输入流
     */
    public GetObjectResponse getObject(String objectName) throws Exception
    {
        return minioClient.getObject(GetObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(objectName)
                .build());
    }

    /**
     * 获取对象元信息
     *
     * @param objectName 对象名称
     * @return 对象元信息
     */
    public StatObjectResponse statObject(String objectName) throws Exception
    {
        return minioClient.statObject(StatObjectArgs.builder()
                .bucket(minioProperties.getBucketName())
                .object(objectName)
                .build());
    }

    private void ensureBucketExists() throws Exception
    {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.getBucketName())
                .build());
        if (!exists)
        {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .build());
        }
    }

    private String buildObjectName(MultipartFile file, String folder)
    {
        LocalDate now = LocalDate.now();
        String extension = FileUploadUtils.getExtension(file);
        String cleanPrefix = trimSlash(minioProperties.getObjectPrefix());
        String cleanFolder = trimSlash(StringUtils.defaultIfBlank(folder, "common"));
        return cleanPrefix + "/" + cleanFolder + "/" + now.getYear() + "/"
                + String.format("%02d", now.getMonthValue()) + "/"
                + String.format("%02d", now.getDayOfMonth()) + "/"
                + IdUtils.fastSimpleUUID() + "." + extension;
    }

    private String getContentType(MultipartFile file)
    {
        return StringUtils.defaultIfBlank(file.getContentType(), "application/octet-stream");
    }

    private String trimSlash(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return "";
        }
        return value.replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
