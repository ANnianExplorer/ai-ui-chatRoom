package com.chat.core.strage.service;

/**
 * 存储服务
 *
 * @author y
 * @since 2026-01-06
 */
public interface StorageService {

    /**
     * 上传文件
     *
     * @param bytes       文件字节数组
     * @param filename    文件名
     * @param contentType 文件类型
     * @return 文件路径
     */
    String upload(byte[] bytes, String filename, String contentType);

}
