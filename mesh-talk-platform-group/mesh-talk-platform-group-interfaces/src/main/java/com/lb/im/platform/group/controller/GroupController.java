package com.lb.im.platform.group.controller;

import com.lb.im.platform.common.model.vo.GroupInviteVO;
import com.lb.im.platform.common.model.vo.GroupMemberVO;
import com.lb.im.platform.common.model.vo.GroupVO;
import com.lb.im.platform.common.response.ResponseMessage;
import com.lb.im.platform.common.response.ResponseMessageFactory;
import com.lb.im.platform.group.appliication.service.GroupService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(tags = "群聊")
@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @ApiOperation(value = "创建群聊", notes = "创建群聊")
    @PostMapping("/create")
    public ResponseMessage<GroupVO> createGroup(@Valid @RequestBody GroupVO vo) {
        return ResponseMessageFactory.getSuccessResponseMessage(groupService.createGroup(vo));
    }

    @ApiOperation(value = "修改群聊信息", notes = "修改群聊信息")
    @PutMapping("/modify")
    public ResponseMessage<GroupVO> modifyGroup(@Valid @RequestBody GroupVO vo) {
        return ResponseMessageFactory.getSuccessResponseMessage(groupService.modifyGroup(vo));
    }

    @ApiOperation(value = "解散群聊", notes = "解散群聊")
    @DeleteMapping("/delete/{groupId}")
    public ResponseMessage deleteGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @ApiOperation(value = "查询群聊", notes = "查询单个群聊信息")
    @GetMapping("/find/{groupId}")
    public ResponseMessage<GroupVO> findGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        return ResponseMessageFactory.getSuccessResponseMessage(groupService.findById(groupId));
    }

    @ApiOperation(value = "查询群聊列表", notes = "查询群聊列表")
    @GetMapping("/list")
    public ResponseMessage<List<GroupVO>> findGroups() {
        return ResponseMessageFactory.getSuccessResponseMessage(groupService.findGroups());
    }

    @ApiOperation(value = "邀请进群", notes = "邀请好友进群")
    @PostMapping("/invite")
    public ResponseMessage invite(@Valid @RequestBody GroupInviteVO vo) {
        groupService.invite(vo);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @ApiOperation(value = "查询群聊成员", notes = "查询群聊成员")
    @GetMapping("/members/{groupId}")
    public ResponseMessage<List<GroupMemberVO>> findGroupMembers(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        return ResponseMessageFactory.getSuccessResponseMessage(groupService.findGroupMembers(groupId));
    }

    @ApiOperation(value = "退出群聊", notes = "退出群聊")
    @DeleteMapping("/quit/{groupId}")
    public ResponseMessage quitGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.quitGroup(groupId);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

    @ApiOperation(value = "踢出群聊", notes = "将用户踢出群聊")
    @DeleteMapping("/kick/{groupId}")
    public ResponseMessage kickGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId,
                                     @NotNull(message = "用户id不能为空") @RequestParam Long userId) {
        groupService.kickGroup(groupId, userId);
        return ResponseMessageFactory.getSuccessResponseMessage();
    }

}
