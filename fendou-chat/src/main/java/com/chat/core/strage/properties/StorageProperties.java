package com.chat.core.strage.properties;

import com.chat.core.strage.enums.StorageTypeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 存储配置项
 *
 * @author y
 * @since 2026-01-06
 */
@ConfigurationProperties(prefix = "storage")
@Data
@Component
public class StorageProperties {

    /**
     * 存储策略
     */
    private StorageTypeEnum strategy = StorageTypeEnum.LOCAL;

    /**
     * 本地存储配置
     */
    private LocalStorageProperties local;



}
