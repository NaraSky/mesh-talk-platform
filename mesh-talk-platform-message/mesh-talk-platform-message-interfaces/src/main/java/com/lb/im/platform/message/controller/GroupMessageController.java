package com.lb.im.platform.message.controller;

import com.lb.im.platform.common.model.dto.GroupMessageDTO;
import com.lb.im.platform.common.response.ResponseMessage;
import com.lb.im.platform.common.response.ResponseMessageFactory;
import com.lb.im.platform.message.application.service.GroupMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "群聊消息")
@RestController
@RequestMapping("/message/group")
public class GroupMessageController {

    @Autowired
    private GroupMessageService groupMessageService;

    @PostMapping("/send")
    @ApiOperation(value = "发送群聊消息", notes = "发送群聊消息")
    public ResponseMessage<Long> sendMessage(@Valid @RequestBody GroupMessageDTO dto) {
        return ResponseMessageFactory.getSuccessResponseMessage(groupMessageService.sendMessage(dto));
    }
}
