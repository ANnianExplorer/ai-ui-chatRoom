package com.chat.domain.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统公告实体类
 *
 * @author y
 * @since 2026-04-02
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SystemAnnouncement extends BasePOJO {

    private String title;
    private String content;
    private Integer onlineCount;
}
