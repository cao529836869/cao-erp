package com.ruoyi.web.controller.common;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.MinioUploadResult;
import com.ruoyi.common.service.MinioStorageService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.framework.config.ServerConfig;
import io.minio.GetObjectResponse;
import io.minio.StatObjectResponse;

/**
 * 通用请求处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/common")
public class CommonController
{
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private MinioStorageService minioStorageService;

    private static final String FILE_DELIMITER = ",";

    /**
     * 通用下载请求
     * 
     * @param fileName 文件名称
     * @param delete 是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request)
    {
        try
        {
            if (!FileUtils.checkAllowDownload(fileName))
            {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = RuoYiConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(filePath);
            }
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", url);
            ajax.put("fileName", fileName);
            ajax.put("newFileName", FileUtils.getName(fileName));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 通用上传请求（多个）
     */
    @PostMapping("/uploads")
    public AjaxResult uploadFiles(List<MultipartFile> files) throws Exception
    {
        try
        {
            // 上传文件路径
            String filePath = RuoYiConfig.getUploadPath();
            List<String> urls = new ArrayList<String>();
            List<String> fileNames = new ArrayList<String>();
            List<String> newFileNames = new ArrayList<String>();
            List<String> originalFilenames = new ArrayList<String>();
            for (MultipartFile file : files)
            {
                // 上传并返回新文件名称
                String fileName = FileUploadUtils.upload(filePath, file);
                String url = serverConfig.getUrl() + fileName;
                urls.add(url);
                fileNames.add(fileName);
                newFileNames.add(FileUtils.getName(fileName));
                originalFilenames.add(file.getOriginalFilename());
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", StringUtils.join(urls, FILE_DELIMITER));
            ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMITER));
            ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMITER));
            ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMITER));
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * MinIO 图片上传请求（单个）
     */
    @PostMapping("/minio/upload/image")
    public AjaxResult uploadMinioImage(MultipartFile file, String folder)
    {
        try
        {
            MinioUploadResult result = minioStorageService.upload(file, StringUtils.defaultIfBlank(folder, "style"), MimeTypeUtils.IMAGE_EXTENSION);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", serverConfig.getUrl() + result.getUrl());
            ajax.put("fileName", result.getUrl());
            ajax.put("newFileName", result.getNewFileName());
            ajax.put("originalFilename", result.getOriginalFilename());
            ajax.put("bucketName", result.getBucketName());
            ajax.put("objectName", result.getObjectName());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * MinIO 文件上传请求（单个）
     */
    @PostMapping("/minio/upload")
    public AjaxResult uploadMinioFile(MultipartFile file, String folder)
    {
        try
        {
            MinioUploadResult result = minioStorageService.upload(file, folder, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", serverConfig.getUrl() + result.getUrl());
            ajax.put("fileName", result.getUrl());
            ajax.put("newFileName", result.getNewFileName());
            ajax.put("originalFilename", result.getOriginalFilename());
            ajax.put("bucketName", result.getBucketName());
            ajax.put("objectName", result.getObjectName());
            return ajax;
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * MinIO 资源预览
     */
    @Anonymous
    @GetMapping("/minio/preview/**")
    public void minioPreview(HttpServletRequest request, HttpServletResponse response)
    {
        String objectName = StringUtils.substringAfter(request.getRequestURI(), "/common/minio/preview/");
        if (StringUtils.isBlank(objectName) || objectName.contains(".."))
        {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try (GetObjectResponse objectResponse = minioStorageService.getObject(objectName))
        {
            StatObjectResponse statObject = minioStorageService.statObject(objectName);
            response.setContentType(StringUtils.defaultIfBlank(statObject.contentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE));
            response.setHeader("Cache-Control", "public, max-age=604800");
            IOUtils.copy(objectResponse, response.getOutputStream());
        }
        catch (Exception e)
        {
            log.error("预览 MinIO 文件失败", e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        try
        {
            if (!FileUtils.checkAllowDownload(resource))
            {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            // 本地资源路径
            String localPath = RuoYiConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + FileUtils.stripPrefix(resource);
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }
}
