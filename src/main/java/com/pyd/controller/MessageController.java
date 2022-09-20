package com.pyd.controller;

import com.pyd.common.Result;
import com.pyd.service.MessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

@Api(tags = "WebSocket消息管理")
@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    MessageService messageService;

    @ApiOperation(value = "展示消息中心消息")
    @GetMapping("/show/{id}")
    public Result showMsg(@PathVariable Long id){
        return Result.succ(messageService.showMsg(id));
    }

    @ApiOperation(value = "删除消息")
    @GetMapping("/delete/{userID}")
    public Result deleteMsg(@PathVariable Long userID){
        return Result.succ(messageService.deleteMsgRec(userID));
    }

    @ApiOperation(value = "已读消息")
    @GetMapping("/read/{msgID}")
    public Result readMsg(@PathVariable Long msgID){
        return Result.succ(messageService.readMsg(msgID));
    }
}
