package com.lb.im.platform.user.application.service;

import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.dto.LoginDTO;
import com.lb.im.platform.common.model.dto.ModifyPwdDTO;
import com.lb.im.platform.common.model.dto.RegisterDTO;
import com.lb.im.platform.common.model.entity.User;
import com.lb.im.platform.common.model.vo.LoginVO;
import com.lb.im.platform.common.model.vo.OnlineTerminalVO;
import com.lb.im.platform.common.model.vo.UserVO;

import java.util.List;

public interface UserService {
    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 用户注册
     */
    void register(RegisterDTO dto);

    /**
     * 用refreshToken换取新 token
     */
    LoginVO refreshToken(String refreshToken) throws IMException;

    /**
     * 修改用户密码
     */
    void modifyPassword(ModifyPwdDTO dto);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User findUserByUserName(String username);

    /**
     * 更新用户信息，好友昵称和群聊昵称等冗余信息也会更新
     *
     * @param vo 用户信息vo
     */
    void update(UserVO vo);

    /**
     * 根据用户昵id查询用户以及在线状态
     *
     * @param id 用户id
     * @return 用户信息
     */
    UserVO findUserById(Long id, boolean constantsOnlineFlag);

    /**
     * 根据用户昵称查询用户，最多返回20条数据
     *
     * @param name 用户名或昵称
     * @return 用户列表
     */
    List<UserVO> findUserByName(String name);

    /**
     * 获取用户在线的终端类型
     *
     * @param userIds 用户id，多个用‘,’分割
     * @return 在线用户终端
     */
    List<OnlineTerminalVO> getOnlineTerminals(String userIds);
}
