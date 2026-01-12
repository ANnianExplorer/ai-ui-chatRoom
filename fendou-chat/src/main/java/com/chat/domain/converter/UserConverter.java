package com.chat.domain.converter;


import com.chat.domain.dto.user.UserProfileDTO;
import com.chat.domain.dto.user.UserRegisterDTO;
import com.chat.domain.pojo.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 用户转换类
 *
 * @author y
 * @since 2026-01-06
 */

@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    /**
     * 将 UserProfileDTO 转换为 User
     *
     * @param userProfileDTO 用户个人信息数据传输对象
     * @return 用户实体类
     */
    User toEntity(UserProfileDTO userProfileDTO);


    /**
     * 将 UserRegisterDTO 转换为 User
     *
     * @param userRegisterDTO 用户注册数据传输对象
     * @return 用户实体类
     */
    User toEntity(UserRegisterDTO userRegisterDTO);
}
