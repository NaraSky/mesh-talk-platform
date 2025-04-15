package com.lb.im.platform.user.controller;

import com.lb.im.platform.common.model.vo.OnlineTerminalVO;
import com.lb.im.platform.common.model.vo.UserVO;
import com.lb.im.platform.common.response.ResponseMessage;
import com.lb.im.platform.common.response.ResponseMessageFactory;
import com.lb.im.platform.common.session.SessionContext;
import com.lb.im.platform.common.session.UserSession;
import com.lb.im.platform.user.application.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Api(tags = "用户")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/terminal/online")
    @ApiOperation(value = "判断用户哪个终端在线",notes="返回在线的用户id的终端集合")
    public ResponseMessage<List<OnlineTerminalVO>> getOnlineTerminal(@NotEmpty @RequestParam("userIds") String userIds){
        return ResponseMessageFactory.getSuccessResponseMessage(userService.getOnlineTerminals(userIds));
    }


    @GetMapping("/self")
    @ApiOperation(value = "获取当前用户信息",notes="获取当前用户信息")
    public ResponseMessage<UserVO> findSelfInfo(){
        UserSession session = SessionContext.getSession();
        UserVO userVO = userService.findUserById(session.getUserId(), false);
        return ResponseMessageFactory.getSuccessResponseMessage(userVO);
    }


    @GetMapping("/find/{id}")
    @ApiOperation(value = "查找用户",notes="根据id查找用户")
    public ResponseMessage<UserVO> findById(@NotEmpty @PathVariable("id") Long id){
        return ResponseMessageFactory.getSuccessResponseMessage(userService.findUserById(id, true));
    }

    @PutMapping("/update")
    @ApiOperation(value = "修改用户信息",notes="修改用户信息，仅允许修改登录用户信息")
    public ResponseMessage<String> update(@Valid @RequestBody UserVO vo){
        userService.update(vo);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @GetMapping("/findByName")
    @ApiOperation(value = "查找用户",notes="根据用户名或昵称查找用户")
    public ResponseMessage<List<UserVO>> findByName(@RequestParam("name") String name){
        return ResponseMessageFactory.getSuccessResponseMessage(userService.findUserByName(name));
    }
}
