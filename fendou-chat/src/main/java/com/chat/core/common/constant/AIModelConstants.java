package com.chat.core.common.constant;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author y
 * @since 2026-01-06
 */

@Component
public class AIModelConstants implements InitializingBean {

    @Value("${ai.model}")
    private String model;
    @Value("${ai.api-key}")
    private String apiKey;
    @Value("${ai.api-url}")
    private String apiUrl;

    public static String MODEL;
    public static String API_KEY;
    public static String API_URL;

    @Override
    public void afterPropertiesSet() throws Exception {
        MODEL = model;
        API_KEY = apiKey;
        API_URL = apiUrl;
    }
}
