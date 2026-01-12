package com.chat.core.common.util;

import com.chat.core.common.constant.CommonConstants;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

/**
 * 头像生成工具
 *
 * @author y
 * @since 2026-01-07
 */
@Slf4j
public class AvatarGeneratorUtils {

    /**
     * 生成头像并转换为Base64编码
     *
     * @param name 用户名称
     * @param size 头像尺寸
     * @return Base64编码的图像数据
     */
    public static String generateAvatarAsBase64(String name, int size) {
        try {
            BufferedImage avatar = generateAvatar(name, size);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(avatar, CommonConstants.DEFAULT_IMAGE_SUFFIX, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            log.error("Failed to generate avatar as base64", e);
            return null;
        }
    }

    /**
     * 将BufferedImage转换为字节数组
     *
     * @param name 用户名称
     * @param size 头像尺寸
     * @return 字节数组
     */
    public static byte[] generateAvatarAsBytes(String name, int size) {
        try {
            BufferedImage bufferedImage = generateAvatar(name, size);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, CommonConstants.DEFAULT_IMAGE_SUFFIX, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to convert image to bytes", e);
            throw new RuntimeException("Failed to convert image to bytes", e);
        }
    }


    public static String generateAvatarAsFile(String name, int size, String outPath) {
        try {
            BufferedImage bufferedImage = generateAvatar(name, size);
            ImageIO.write(bufferedImage, CommonConstants.DEFAULT_IMAGE_SUFFIX, new File(outPath));

            return outPath;
        } catch (IOException e) {
            log.error("Failed to generate avatar as file", e);
            return null;
        }
    }


    /**
     * 生成默认头像
     *
     * @param name 用户名称
     * @param size 头像尺寸
     * @return BufferedImage 头像图像
     */
    public static BufferedImage generateAvatar(String name, int size) {
        // 提取首字母
        String initials = extractInitials(name);

        // 根据名称确定背景颜色
        String backgroundColor = getBackgroundColorByName(name);

        // 创建图像
        return createAvatarImage(initials, backgroundColor, size);
    }

    /**
     * 提取用户名称首字母
     *
     * @param name 用户名称
     * @return 首字母字符串
     */
    private static String extractInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U"; // 默认用户标识
        }

        StringBuilder initials = new StringBuilder();
        String[] words = name.trim().split("\\s+");

        for (int i = 0; i < Math.min(2, words.length); i++) {
            if (!words[i].isEmpty()) {
                initials.append(Character.toUpperCase(words[i].charAt(0)));
            }
        }

        return initials.toString();
    }

    /**
     * 根据名称哈希值获取背景颜色
     *
     * @param name 用户名称
     * @return 背景颜色十六进制值
     */
    private static String getBackgroundColorByName(String name) {
        if (name == null || name.isEmpty()) {
            return CommonConstants.DEFAULT_BACKGROUND_COLORS[0];
        }

        int hash = Math.abs(name.hashCode());
        return CommonConstants.DEFAULT_BACKGROUND_COLORS[hash % CommonConstants.DEFAULT_BACKGROUND_COLORS.length];
    }

    /**
     * 创建头像图像
     *
     * @param initials        首字母
     * @param backgroundColor 背景颜色
     * @param size            图像尺寸
     * @return BufferedImage 头像图像
     */
    private static BufferedImage createAvatarImage(String initials, String backgroundColor, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 绘制圆形背景
        g2d.setColor(Color.decode(backgroundColor));
        g2d.fillOval(0, 0, size, size);

        // 设置文字属性
        g2d.setColor(Color.WHITE);
        int fontSize = (int) (size * 0.4);
        Font font = new Font(CommonConstants.DEFAULT_FONT_FAMILY, Font.BOLD, fontSize);
        g2d.setFont(font);

        // 计算文字位置使其居中
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(initials);
        int textHeight = fm.getAscent() - fm.getLeading();

        int x = (size - textWidth) / 2;
        int y = (size + textHeight) / 2;

        // 绘制文字
        g2d.drawString(initials, x, y);

        g2d.dispose();
        return image;
    }
}
