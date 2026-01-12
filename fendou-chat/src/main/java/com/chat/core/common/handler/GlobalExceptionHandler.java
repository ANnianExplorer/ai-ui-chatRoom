package com.chat.core.common.handler;

import com.chat.core.common.excpetion.CustomException;
import com.chat.core.common.rersult.Result;
import com.chat.core.common.rersult.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

/**
 * @author y
 * @since 2026-01-05
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 全局异常处理
     *
     * @param e 异常
     * @return 结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> error(Exception e) {
        log.error("全局异常处理-->{}", e.getMessage());
        e.printStackTrace();
        return Result.fail(ResultCodeEnum.SERVER_ERROR);
    }


    /**
     * 参数校验异常
     *
     * @param e 异常
     * @return 结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> error(MethodArgumentNotValidException e) {
        log.error("参数异常处理-->{}", e.getMessage());
        e.printStackTrace();
        // 获取校验结果
        BindingResult bindingResult = e.getBindingResult();
        // 获取校验错误信息
        return Result.fail().message(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
    }


    /**
     * 自定义异常
     *
     * @param e 异常
     * @return 结果
     */
    @ExceptionHandler(CustomException.class)
    public Result<Object> error(CustomException e) {
        log.error("自定义异常处理-->{}", e.getMessage());
        e.printStackTrace();
        return Result.fail().message(e.getMessage()).code(e.getCode());
    }
}
