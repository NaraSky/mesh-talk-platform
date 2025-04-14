package com.lb.im.platform.user.controller;

import com.lb.im.platform.common.model.dto.LoginDTO;
import com.lb.im.platform.common.model.dto.RegisterDTO;
import com.lb.im.platform.common.model.vo.LoginVO;
import com.lb.im.platform.common.response.ResponseMessage;
import com.lb.im.platform.common.response.ResponseMessageFactory;
import com.lb.im.platform.user.application.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "用户登录和注册")
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseMessage<LoginVO> register(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = userService.login(dto);
        return ResponseMessageFactory.getSuccessResponseMessage(vo);
    }

    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "用户注册")
    public ResponseMessage<String> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

}
