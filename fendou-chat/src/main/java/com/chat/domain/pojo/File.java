package com.chat.domain.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 聊天文件实体类
 *
 * @author y
 * @since 2026-01-07
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class File extends BasePOJO {

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 存储文件名
     */
    private String storageName;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件大小(字节)
     */
    private Integer fileSize;

    /**
     * 文件创建者
     */
    private Integer createBy;
    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件MD5值
     */
    private String md5;

}