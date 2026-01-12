package com.chat.core.strage.config;

import com.chat.core.strage.enums.StorageTypeEnum;
import com.chat.core.strage.properties.StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 本地资源配置
 *
 * @author y
 * @since 2026-01-06
 */

@Configuration
public class LocalResourceConfig implements WebMvcConfigurer {

    @Autowired
    private StorageProperties storageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if(storageProperties.getStrategy() != StorageTypeEnum.LOCAL){
            return;
        }

        String path = storageProperties.getLocal().getPath();
        String startPath = storageProperties.getLocal().getStartPath();
        registry.addResourceHandler("/" + startPath + "/**")
                .addResourceLocations("file:" + path + "/" + startPath + "/");
    }
}
