package com.pyd.controller;

import com.pyd.common.ParamDto;
import com.pyd.common.Result;
import com.pyd.mapper.DocMapper;
import com.pyd.service.DocService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@Api(tags = "文档管理模块")
@RestController
@RequestMapping("/docs")

public class DocController {
    @Autowired
    DocMapper docMapper;

    @Autowired
    DocService docService;

    @ApiOperation(value = "获取文件夹文件目录")
    @PostMapping("/folder")
    public Result getFolderDocs(@RequestBody ParamDto paramDto){
        return Result.succ(docService.getFolderDocs(paramDto.getCurrentPage(), paramDto.getPageSize(), paramDto.getId()));
    }

    @ApiOperation(value = "关键词搜索")
    @PostMapping("/keySearch")
    public Result searchDocs(@RequestBody ParamDto paramDto){
        if (!StringUtils.hasText(paramDto.getTexts())){
            return Result.fail("请输入搜索内容！");
        }else {
            return Result.succ(docService.searchDocs(paramDto.getCurrentPage(), paramDto.getPageSize(), paramDto.getTexts()));
        }
    }

    @ApiOperation(value = "预览文件")
    @GetMapping("/preview/{id}")
    public Result previewDoc(@PathVariable String id){
        return Result.succ(docService.previewDoc(Long.parseLong(id)));
    }

    @ApiOperation(value = "编辑文件")
    @PostMapping("/edit")
    public Result editDoc(@RequestBody ParamDto paramDto){
        String[] res = docService.editDoc(paramDto.getId());
        if (res.length == 1){
            return Result.fail("该文件不支持编辑!");
        }else {
            return Result.succ(res);
        }
    }

    @PostMapping("/saveFile/{id}")
    @ApiOperation("在线编辑保存回调接口")
    @ResponseBody
    public HashMap<String, Integer> saveFile(HttpServletRequest request , HttpServletResponse response,
                                             @PathVariable String id, @RequestBody  String st) throws IOException {
        String[] ids = id.split(",");
        String docID = ids[0];
        String userName = ids[1];
        return docService.saveFile(request, response, docID, userName, st);
    }

    @ApiOperation(value = "高级检索")
    @PostMapping("/search")
    public Result detailSearch(@RequestBody ParamDto paramDto){
        return Result.succ(docService.detailSearch(paramDto.getCurrentPage(), paramDto.getPageSize(), paramDto.getUploader(), paramDto.getSender(),
                paramDto.getType(), paramDto.getTime()));
    }

    @ApiOperation(value = "预览版本记录文档")
    @GetMapping("/previewVer/{id}")
    public Result previewVerDoc(@PathVariable String id){
        return Result.succ(docService.previewVerDoc(id));
    }
}
