package com.chat.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chat.core.common.util.SecurityHolderUtils;
import com.chat.domain.pojo.File;
import com.chat.core.common.excpetion.CustomException;
import com.chat.mapper.FileMapper;
import com.chat.service.FileService;
import com.chat.core.strage.service.StorageService;
import com.chat.core.common.rersult.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author y
 * @since 2026-01-07
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File>
        implements FileService {

    @Autowired
    private StorageService storageService;


    @Override
    public File upload(MultipartFile multipartFile) {
        try {
            return upload(multipartFile.getBytes(), multipartFile.getOriginalFilename(), multipartFile.getContentType());
        } catch (Exception e) {
            throw new CustomException(ResultCodeEnum.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public File upload(byte[] bytes, String filename, String contentType) {
        try {
            long size = bytes.length;

            // 判断文件大小
            if (size > 1024 * 1024 * 10) {
                throw new CustomException(ResultCodeEnum.FILE_TOO_LARGE);
            }

            // 获取文件名
            String md5 = RandomUtil.randomString(12);
            String generateFilename = generateFilename(filename, md5);

            String filePath = storageService.upload(bytes, generateFilename, contentType);

            // 获取当前登录用户
            Integer currentUserId = SecurityHolderUtils.getUserId();

            // 保存文件信息
            File file = new File();
            file.setOriginalName(filename);
            file.setStorageName(generateFilename);
            file.setCreateBy(currentUserId); // 注册时为null，数据库中该字段应该允许null
            file.setFilePath(filePath);
            file.setFileSize((int) size);
            file.setFileType(contentType);
            file.setMd5(md5);
            this.save(file);

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ResultCodeEnum.FILE_UPLOAD_ERROR);
        }
    }

    @Override
    public void download(String fileName) {

    }


    /**
     * 传入文件名返回新的文件名
     *
     * @param fileName 文件名
     * @return 新的文件名
     */
    public static String generateFilename(String fileName, String md5) {
        if (fileName == null) {
            throw new CustomException(ResultCodeEnum.FILE_NOT_FIND);
        }

        // 获取加密的文件名
        String md5FileName = DigestUtil.md5Hex(fileName + md5);

        // 获取文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        // 返回新的文件名
        return md5FileName + suffix;
    }
}
