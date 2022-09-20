package com.pyd.controller;


import com.pyd.common.Result;
import com.pyd.service.TransferService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

@Api(tags = "流转记录模块")
@RestController
@RequestMapping("/transfer")
public class TransferController {
    @Autowired
    TransferService transferService;

    @ApiOperation(value = "展示文件流转记录")
    @GetMapping("/show/{nowID}")
    public Result showTransfer(@PathVariable Long nowID){
        return Result.succ(transferService.showTransfer(nowID));
    }
}
