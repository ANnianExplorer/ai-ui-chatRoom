package com.chat.domain.dto.ai;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * @author y
 * @since 2026/4/2
 **/
@Data
public class QueryFriends {
    @ToolParam(required = false, description = "当前用户id")
    private Integer userId;

    @ToolParam(required = false, description = "好友状态，启用-禁用")
    private Integer status;
    @ToolParam(required = false, description = "好友是否存在")
    private Integer isDeleted;
    @ToolParam(required = false, description = "排序方式")
    private List<Sort> sorts;

    @Data
    public static class Sort {
        @ToolParam(required = false, description = "排序字段：friend_id")
        private String field;
        @ToolParam(required = false, description = "是否是升序：true/false")
        private Boolean asc;
    }
}
