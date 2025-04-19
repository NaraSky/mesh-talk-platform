package com.lb.im.platform.message.controller;

import com.lb.im.platform.common.model.dto.PrivateMessageDTO;
import com.lb.im.platform.common.model.vo.PrivateMessageVO;
import com.lb.im.platform.common.response.ResponseMessage;
import com.lb.im.platform.common.response.ResponseMessageFactory;
import com.lb.im.platform.message.application.service.PrivateMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

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

    @PostMapping("/pullUnreadMessage")
    @ApiOperation(value = "拉取未读消息", notes = "拉取未读消息")
    public ResponseMessage<String> pullUnreadMessage() {
        privateMessageService.pullUnreadMessage();
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @GetMapping("/loadMessage")
    @ApiOperation(value = "拉取消息", notes = "拉取消息,一次最多拉取100条")
    public ResponseMessage<List<PrivateMessageVO>> loadMessage(@RequestParam Long minId) {
        return ResponseMessageFactory.getSuccessResponseMessage(privateMessageService.loadMessage(minId));
    }

    @GetMapping("/history")
    @ApiOperation(value = "查询聊天记录", notes = "查询聊天记录")
    public ResponseMessage<List<PrivateMessageVO>> recallMessage(@NotNull(message = "好友id不能为空") @RequestParam Long friendId,
                                                                 @NotNull(message = "页码不能为空") @RequestParam Long page,
                                                                 @NotNull(message = "size不能为空") @RequestParam Long size) {
        return ResponseMessageFactory.getSuccessResponseMessage(privateMessageService.getHistoryMessage(friendId, page, size));
    }
}
