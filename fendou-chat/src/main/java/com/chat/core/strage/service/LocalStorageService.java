package com.chat.core.strage.service;

import com.chat.core.common.excpetion.CustomException;
import com.chat.core.strage.properties.LocalStorageProperties;
import com.chat.core.common.rersult.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地存储服务
 *
 * @author y
 * @since 2026-01-06
 */
@Service
@ConditionalOnProperty(prefix = "storage", name = "strategy", havingValue = "local")
public class LocalStorageService implements StorageService {

    @Autowired
    private LocalStorageProperties localStorageProperties;

    @Override
    public String upload(byte[] bytes, String filename, String contentType) {
        try {
            // 创建目录
            Path rootLocation = Paths.get(localStorageProperties.getPath(), localStorageProperties.getStartPath());
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }

            // 保存文件
            Path destinationFile = rootLocation.resolve(filename);
            Files.write(destinationFile, bytes);

            // 返回访问路径
            return localStorageProperties.getDomain() + "/" + localStorageProperties.getStartPath() + "/" + filename;
        } catch (IOException e) {
            throw new CustomException(ResultCodeEnum.FILE_UPLOAD_ERROR);
        }
    }
}
