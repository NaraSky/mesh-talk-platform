package com.lb.im.platform.message.controller;

import com.lb.im.platform.common.model.dto.PrivateMessageDTO;
import com.lb.im.platform.common.response.ResponseMessage;
import com.lb.im.platform.common.response.ResponseMessageFactory;
import com.lb.im.platform.message.application.service.PrivateMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "私聊消息")
@RestController
@RequestMapping("/message/private")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;

    @PostMapping("/send")
    @ApiOperation(value = "发送消息", notes = "发送私聊消息")
    public ResponseMessage<Long> sendMessage(@Valid @RequestBody PrivateMessageDTO dto) {
        return ResponseMessageFactory.getSuccessResponseMessage(privateMessageService.sendMessage(dto));
    }
}
