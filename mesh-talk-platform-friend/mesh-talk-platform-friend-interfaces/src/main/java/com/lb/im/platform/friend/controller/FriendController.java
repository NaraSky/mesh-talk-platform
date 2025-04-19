package com.lb.im.platform.friend.controller;

import com.lb.im.platform.common.model.vo.FriendVO;
import com.lb.im.platform.common.response.ResponseMessage;
import com.lb.im.platform.common.response.ResponseMessageFactory;
import com.lb.im.platform.common.session.SessionContext;
import com.lb.im.platform.friend.application.service.FriendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Api(tags = "好友")
@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @GetMapping("/list")
    @ApiOperation(value = "好友列表", notes = "获取好友列表")
    public ResponseMessage<List<FriendVO>> findFriends() {
        List<FriendVO> friends = friendService.findFriendByUserId(SessionContext.getSession().getUserId());
        return ResponseMessageFactory.getSuccessResponseMessage(friends);
    }

    @PostMapping("/add")
    @ApiOperation(value = "添加好友", notes = "双方建立好友关系")
    public ResponseMessage addFriend(@NotEmpty(message = "好友id不可为空") @RequestParam("friendId") Long friendId) {
        friendService.addFriend(friendId);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @GetMapping("/find/{friendId}")
    @ApiOperation(value = "查找好友信息", notes = "查找好友信息")
    public ResponseMessage<FriendVO> findFriend(@NotEmpty(message = "好友id不可为空") @PathVariable("friendId") Long friendId) {
        return ResponseMessageFactory.getSuccessResponseMessage(friendService.findFriend(friendId));
    }


    @DeleteMapping("/delete/{friendId}")
    @ApiOperation(value = "删除好友", notes = "解除好友关系")
    public ResponseMessage delFriend(@NotEmpty(message = "好友id不可为空") @PathVariable("friendId") Long friendId) {
        friendService.delFriend(friendId);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @PutMapping("/update")
    @ApiOperation(value = "更新好友信息", notes = "更新好友头像或昵称")
    public ResponseMessage modifyFriend(@Valid @RequestBody FriendVO vo) {
        friendService.update(vo);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }
}
