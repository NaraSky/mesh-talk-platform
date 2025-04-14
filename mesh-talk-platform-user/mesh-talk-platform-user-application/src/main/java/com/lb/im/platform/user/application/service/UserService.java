package com.lb.im.platform.user.application.service;

import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.dto.LoginDTO;
import com.lb.im.platform.common.model.dto.RegisterDTO;
import com.lb.im.platform.common.model.vo.LoginVO;

public interface UserService {
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 用户注册
     */
    void register(RegisterDTO dto);
}
