package com.chat.core.common.constant;

/**
 * 公共常量
 *
 * @author y
 * @since 2026-01-06
 */
public interface CommonConstants {

    /**
     * 默认背景颜色
     */
    String[] DEFAULT_BACKGROUND_COLORS = {
            "#FF5722", "#03A9F4", "#4CAF50", "#FF9800",
            "#9C27B0", "#E91E63", "#673AB7", "#3F51B5"
    };

    /**
     * 默认字体
     */
    String DEFAULT_FONT_FAMILY = "Arial, sans-serif";

    /**
     * 默认图片大小
     */
    Integer DEFAULT_IMAGE_SIZE = 100;

    /**
     * 默认图片后缀
     */
    String DEFAULT_IMAGE_SUFFIX = "png";

    /**
     * 默认图片类型
     */
    String DEFAULT_IMAGE_TYPE = "image/png";

    Integer ACTIVE_STATUS = 1;
    Integer DISABLED_STATUS = 0;

    Integer NOT_DELETED_STATUS = 0;

}
