package com.chat.core.strage.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地存储配置项
 *
 * @author y
 * @since 2026-01-06
 */
@ConfigurationProperties(prefix = "storage.local")
@Component
@Data
public class LocalStorageProperties {

    /**
     * 域名
     */
    private String domain = "";

    /**
     * 本地存储路径
     */
    private String path = "";


    /**
     * 起始路径
     */
    private String startPath = "uploads";

}
