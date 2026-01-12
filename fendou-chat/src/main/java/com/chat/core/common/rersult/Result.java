package com.chat.core.common.rersult;


import lombok.Data;

/**
 * 全局统一返回结果类
 *  @author y
 *  @since 2026-01-05
 */
@Data
public class Result<T> {

    // 状态码
    private Integer code;
    // 错误信息
    private String message;
    // 返回数据
    private T data;


    // --------------  操作成功  ---------------

    /**
     * 无数据返回操作成功 Result
     */
    public static <T> Result<T> ok() {
        return Result.ok(null);
    }

    /**
     * 有数据返回操作成功 Result
     */
    public static <T> Result<T> ok(T data) {
        return build(data, ResultCodeEnum.SUCCESS);
    }


    // ------------------ 操作失败 ------------------

    /**
     * 无数据返回操作失败 Result
     */
    public static <T> Result<T> fail() {
        return Result.fail((T) null);
    }

    /**
     * 有数据返回操作失败 Result
     */
    public static <T> Result<T> fail(T data) {
        return build(data, ResultCodeEnum.FAIL);
    }

    /**
     * 带错误信息返回操作失败 Result
     */
    public static <T> Result<T> fail(ResultCodeEnum codeEnum) {
        return build(null, codeEnum);
    }


    // ---------------  链式操作 ------------------

    public Result<T> message(String msg) {
        this.setMessage(msg);
        return this;
    }

    public Result<T> code(Integer code) {
        this.setCode(code);
        return this;
    }

    // ----------- 私有方法 ------------

    /**
     * 构建带数据的 Result 对象
     */
    private static <T> Result<T> build(T data) {
        Result<T> result = new Result<T>();
        if (data != null) {
            result.setData(data);
        }
        return result;
    }

    /**
     * 构建带数据，带错误信息的 Result 对象
     */
    private static <T> Result<T> build(T body, ResultCodeEnum resultCodeEnum) {
        Result<T> result = build(body);
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }
}
