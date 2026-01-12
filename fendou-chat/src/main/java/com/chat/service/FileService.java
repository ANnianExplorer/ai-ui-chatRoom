package com.chat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chat.domain.pojo.File;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author y
 * @since 2026-01-07
 */
public interface FileService extends IService<File> {
    /**
     * 上传文件
     *
     * @param file 文件
     * @return 文件地址
     */
    File upload(MultipartFile file);


    /**
     * 上传文件
     *
     * @param bytes       文件字节数组
     * @param fileName    文件名
     * @param contentType 文件类型
     * @return 文件地址
     */
    File upload(byte[] bytes, String fileName, String contentType);


    /**
     * 下载文件
     *
     * @param fileName 文件名
     */
    void download(String fileName);
}
