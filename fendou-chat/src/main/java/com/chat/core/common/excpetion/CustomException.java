package com.chat.core.common.excpetion;

import com.chat.core.common.rersult.ResultCodeEnum;
import lombok.Getter;

/**
 * 自定义异常
 *
 * @author y
 * @since 2026-01-05
 */
@Getter
public class CustomException extends RuntimeException {

    private Integer code;

    public CustomException(String message) {
        super(message);
    }

    public CustomException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }
}
