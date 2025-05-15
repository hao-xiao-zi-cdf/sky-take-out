package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-15
 * Time: 16:17
 */
public interface UserService {

    /**
     * 使用微信登录功能实现用户登录模块
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);
}
