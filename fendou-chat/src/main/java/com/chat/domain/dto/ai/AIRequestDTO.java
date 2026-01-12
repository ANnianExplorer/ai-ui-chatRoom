package com.chat.domain.dto.ai;

import lombok.Data;

/**
 * AI 请求数据传输对象
 *
 * @author y
 * @since 2026-01-07
 */

@Data
public class AIRequestDTO {

    /**
     * 提示
     */
    private String prompt;

    /**
     * 是否深度思考 true: enabled， false: disabled
     */
    private boolean thinking;


    /**
     * 是否结合历史聊天
     */
    private boolean history;


}
