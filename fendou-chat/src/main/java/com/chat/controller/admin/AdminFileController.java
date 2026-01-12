package com.chat.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.domain.pojo.File;
import com.chat.service.FileService;
import com.chat.core.common.rersult.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员文件控制器
 *
 * @author y
 * @since 2026-01-09
 */
@RequestMapping("/admin/files")
@RestController
public class AdminFileController {

    @Autowired
    private FileService fileService;

    /**
     * 获取文件列表
     *
     * @param page     当前页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词
     * @param type     文件类型
     * @return 文件列表
     */
    @GetMapping
    public Result<?> getFiles(@RequestParam(name = "page", defaultValue = "1") Integer page,
                             @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
                             @RequestParam(name = "keyword", required = false) String keyword,
                             @RequestParam(name = "type", required = false) String type) {
        // 构建查询条件
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        
        // 处理搜索关键词
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(File::getOriginalName, keyword)
                       .or().like(File::getFilePath, keyword);
        }
        
        // 处理文件类型筛选
        if (type != null && !type.isEmpty()) {
            if ("image".equals(type)) {
                // 筛选图片类型文件，MIME类型以image/开头
                queryWrapper.likeRight(File::getFileType, "image/");
            } else if ("document".equals(type)) {
                // 筛选文档类型文件，MIME类型不以image/开头
                queryWrapper.notLikeRight(File::getFileType, "image/");
            }
        }
        
        Page<File> filePage = fileService.page(new Page<>(page, pageSize), queryWrapper);

        // 转换为前端期望的格式
        Map<String, Object> result = new HashMap<>();
        result.put("list", filePage.getRecords());
        result.put("total", filePage.getTotal());

        return Result.ok(result);
    }

    /**
     * 获取文件详情
     *
     * @param id 文件ID
     * @return 文件详情
     */
    @GetMapping("/{id}")
    public Result<?> getFileById(@PathVariable("id") Long id) {
        File file = fileService.getById(id);
        return Result.ok(file);
    }

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteFile(@PathVariable("id") Long id) {
        boolean deleted = fileService.removeById(id);
        return deleted ? Result.ok() : Result.fail();
    }

    /**
     * 批量删除文件
     *
     * @return 是否删除成功
     */
    @DeleteMapping("/batch")
    public Result<?> batchDeleteFiles(@RequestBody BatchDeleteRequest request) {
        boolean deleted = fileService.removeByIds(request.getIds());
        return deleted ? Result.ok() : Result.fail();
    }

    /**
     * 批量删除请求DTO
     */
    public static class BatchDeleteRequest {
        private List<Long> ids;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
    }
}
