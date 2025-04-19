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

/**
 * 私聊消息控制器
 * 提供私聊消息相关的RESTful API接口
 * 处理消息发送、拉取、查询历史记录和撤回等操作
 */
@Api(tags = "私聊消息")
@RestController
@RequestMapping("/message/private")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;

    /**
     * 发送私聊消息
     * 将消息发送给指定好友，并返回生成的消息ID
     * 
     * @param dto 私聊消息数据传输对象，包含接收者ID、消息内容和消息类型等信息
     * @return 包含生成的消息ID的响应对象
     */
    @PostMapping("/send")
    @ApiOperation(value = "发送消息", notes = "发送私聊消息")
    public ResponseMessage<Long> sendMessage(@Valid @RequestBody PrivateMessageDTO dto) {
        return ResponseMessageFactory.getSuccessResponseMessage(privateMessageService.sendMessage(dto));
    }

    /**
     * 拉取未读私聊消息
     * 获取当前用户的所有未读私聊消息，并通过WebSocket异步推送给用户
     * 用于用户上线时或定时拉取未读消息
     * 
     * @return 操作成功的响应对象
     */
    @PostMapping("/pullUnreadMessage")
    @ApiOperation(value = "拉取未读消息", notes = "拉取未读消息")
    public ResponseMessage<String> pullUnreadMessage() {
        privateMessageService.pullUnreadMessage();
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    /**
     * 加载消息历史记录
     * 增量拉取指定ID之后的私聊消息，限制为最近1个月内的消息
     * 用于客户端初始化或滚动加载更多消息
     * 
     * @param minId 最小消息ID，用于增量拉取，返回ID大于此值的消息
     * @return 包含消息历史记录列表的响应对象，最多返回100条
     */
    @GetMapping("/loadMessage")
    @ApiOperation(value = "拉取消息", notes = "拉取消息,一次最多拉取100条")
    public ResponseMessage<List<PrivateMessageVO>> loadMessage(@RequestParam Long minId) {
        return ResponseMessageFactory.getSuccessResponseMessage(privateMessageService.loadMessage(minId));
    }

    /**
     * 查询与指定好友的历史聊天记录
     * 分页获取与特定好友的历史私聊消息
     * 
     * @param friendId 好友用户ID，必填参数
     * @param page 页码，从1开始，必填参数
     * @param size 每页大小，必填参数
     * @return 包含历史消息列表的响应对象
     */
    @GetMapping("/history")
    @ApiOperation(value = "查询聊天记录", notes = "查询聊天记录")
    public ResponseMessage<List<PrivateMessageVO>> getHistoryMessage(@NotNull(message = "好友id不能为空") @RequestParam Long friendId,
                                                                 @NotNull(message = "页码不能为空") @RequestParam Long page,
                                                                 @NotNull(message = "size不能为空") @RequestParam Long size) {
        return ResponseMessageFactory.getSuccessResponseMessage(privateMessageService.getHistoryMessage(friendId, page, size));
    }

    /**
     * 撤回私聊消息
     * 将指定ID的消息标记为已撤回状态
     * 撤回的消息在客户端会显示为"消息已撤回"
     * 
     * @param id 要撤回的消息ID，必填参数
     * @return 操作成功的响应对象
     */
    @DeleteMapping("/recall/{id}")
    @ApiOperation(value = "撤回消息", notes = "撤回私聊消息")
    public ResponseMessage<Long> recallMessage(@NotNull(message = "消息id不能为空") @PathVariable Long id) {
        privateMessageService.recallMessage(id);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }
}
