package com.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.domain.pojo.File;
import com.chat.service.FileService;
import com.chat.core.common.rersult.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件控制器
 *
 * @author y
 * @since 2026-01-07
 */

@RestController
@RequestMapping("file")
public class FileController {

    @Autowired
    private FileService fileService;


    @PostMapping("upload")
    public Result<?> upload(@RequestPart("file") MultipartFile file) {
        return Result.ok(fileService.upload(file));
    }

    @GetMapping("/details/{id}")
    public Result<?> getFileDetailByID(@PathVariable("id") Integer id) {
        File file = fileService.getById(id);
        return Result.ok(file);
    }

    @GetMapping("/detail/{createById}")
    public Result<?> getFileDetail(@PathVariable("createById") Integer createById) {
        // 获得所有匹配的 文件
        LambdaQueryWrapper<File> fileLambdaQueryWrapper = new LambdaQueryWrapper<>();
        fileLambdaQueryWrapper.eq(File::getCreateBy, createById);
        List<File> fileList = fileService.list(fileLambdaQueryWrapper);
        return Result.ok(fileList);
    }
}
