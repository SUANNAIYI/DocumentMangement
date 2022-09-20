package com.pyd.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.pyd.common.ParamDto;
import com.pyd.common.Result;
import com.pyd.entity.Version;
import com.pyd.mapper.DocMapper;
import com.pyd.mapper.VersionMapper;
import com.pyd.service.VersionService;
import com.pyd.util.FileUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Api(tags = "版本记录模块")
@RestController
@RequestMapping("/version")
public class VersionController {

    @Autowired
    VersionService versionService;

    @Autowired
    DocMapper docMapper;

    @Autowired
    VersionMapper versionMapper;

    @ApiOperation(value = "查看版本记录")
    @GetMapping("/{id}")
    public Result showVersion(@PathVariable Long id){
        return Result.succ(versionService.showVersion(id));
    }

    @ApiOperation(value = "删除版本记录")
    @GetMapping("/delete/{ids}")
    public Result deleteVersion(@PathVariable String ids){
        String[] versionIDS = ids.split(",");
        QueryWrapper<Version> versionQueryWrapper = new QueryWrapper<>();
        versionQueryWrapper.in("id", versionIDS).orderByDesc("id");
        List<Version> versionList = versionMapper.selectList(versionQueryWrapper);
        String path = versionList.get(0).getPath();
        Long docID = versionList.get(0).getDocID();
        String rawDocPath = docMapper.selectById(docID).getPath();
        if (path.equals(rawDocPath)){
            return Result.fail("最新版本不能被删除！");
        }else {
            return Result.succ(versionService.deleteVersionRec(ids));
        }
    }
}
