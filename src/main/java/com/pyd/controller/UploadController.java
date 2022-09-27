package com.pyd.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pyd.common.ParamDto;
import com.pyd.common.Result;
import com.pyd.entity.Doc;
import com.pyd.mapper.DocMapper;
import com.pyd.service.DocService;
import com.pyd.service.FolderService;
import com.pyd.service.UploadService;
import com.pyd.util.ShiroUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static com.pyd.service.impl.FolderServiceImpl.*;


@Api(tags = "文件流转模块")
@RestController
@RequestMapping("/upload")
public class UploadController {
    @Autowired
    private UploadService uploadService;

    @Autowired
    private DocService docService;

    @Autowired
    FolderService folderService;

    @Autowired
    DocMapper docMapper;

    @ApiOperation(value = "上传文件")
    @PostMapping
    public Result upload(@RequestPart("file") MultipartFile[] files, @RequestParam String folder) throws Exception{
        Long id;
        HashSet<String> res = new HashSet<>();
        Long receiveFolder = docService.searchFolder("接收文件", ShiroUtil.getProfile().getId());
        Long sendFolder = docService.searchFolder("发送文件", ShiroUtil.getProfile().getId());
        if (folder.contains(",")){
            id = Long.valueOf(folder.substring(folder.lastIndexOf(",") + 1));
        }else {
            id = Long.valueOf(folder);
        }
        // 基本文件夹下不允许上传文件
        if (id.equals(personFolder) || id.equals(releaseFolder) || id.equals(sendFolder) || id.equals(receiveFolder)){
            return Result.fail("该目录下不允许上传文件，请上传至其他目录！");
        }else {
            for (MultipartFile file : files){
                res.add(uploadService.uploadFile(file, id));
            }
            if (res.contains("上传文件类型不支持！")){
                if (files.length == 1){
                    return Result.fail("上传文件类型不支持!");
                }else {
                    return Result.fail("部分上传文件类型不支持!");
                }
            }else {
                return Result.succ("上传成功");
            }
        }
    }

    @ApiOperation(value = "通过id批量删除文件")
    @GetMapping("/delete/{ids}")
    public Result deleteFile(@PathVariable String ids){
        String[] idList = ids.split(",");  // 获取id列表
        // 循环删除记录
        for (String i : idList){
            Long id = Long.valueOf(i);
            docService.deleteFileRec(id);
        }
        return Result.succ("删除成功");
    }

    @ApiOperation(value = "通过id批量下载文件")
    @GetMapping("/download/{ids}")
    public ResponseEntity<byte[]> downloadFile(HttpServletResponse response, @PathVariable String ids) throws IOException{
        return uploadService.downloadFileByIds(ids, response);
    }

    @ApiOperation(value = "通过用户id发送文件")
    @PostMapping("/send")
    public Result sendFiles(@RequestBody ParamDto paramDto)throws IOException{
        if (!StringUtils.hasText(paramDto.getUserIds())){
            return Result.fail("请选择用户");
        }else {
            return Result.succ(uploadService.sendFiles(paramDto.getUserIds(), paramDto.getDocIds()));
        }
    }

    @ApiOperation(value = "发布文件")
    @PostMapping("/release")
    public Result releaseFiles(@RequestBody ParamDto paramDto)throws IOException{
        return Result.succ(uploadService.releaseFiles(paramDto.getId()));
    }

    @ApiOperation(value = "共享文件")
    @PostMapping("/share")
    public Result shareDoc(@RequestBody ParamDto paramDto)throws IOException{
        return Result.succ(uploadService.shareDoc(paramDto.getId()));
    }

    @ApiOperation(value = "退回文件")
    @PostMapping("/back")
    public Result backDocs(@RequestBody ParamDto paramDto) throws IOException{
        // 管理员权限
        if (!ShiroUtil.getProfile().getRole().equals("admin")){
            return Result.fail("您没有权限！");
        }else {
            HashSet<String> res = new HashSet<>();  // 返回结果
            String type = paramDto.getType();
            String[] id = paramDto.getDocIds().split(",");
            QueryWrapper<Doc> docQueryWrapper = new QueryWrapper<>();
            docQueryWrapper.in("id", id);
            List<Doc> docList = docMapper.selectList(docQueryWrapper);
            for (Doc doc : docList){
                res.add(uploadService.backDocs(doc, type));
            }
            // 判断是否退回失败
            if (res.contains("该文件不可退回")){
                if (docList.size() > 1){
                    return Result.fail("部分文件不可退回");
                }else {
                    return Result.fail("该文件不可退回");
                }
            }else if (res.contains("请审核后再退回")) {
                return Result.fail("文件请审核后退回");
            }else {
                return Result.succ("退回成功");
            }
        }
    }

    @ApiOperation(value = "审核文件")
    @PostMapping("/review")
    public Result reviewDoc(@RequestBody ParamDto paramDto){
        if (!StringUtils.hasText(paramDto.getUserIds())){
            return Result.fail("请选择用户");
        }else {
            return Result.succ(uploadService.reviewDoc(Long.parseLong(paramDto.getDocIds()), Long.parseLong(paramDto.getUserIds())));
        }
    }

    @ApiOperation(value = "收藏文件")
    @GetMapping("/collect/{docID}")
    public Result collectDoc(@PathVariable Long docID) throws IOException{
        return Result.succ(uploadService.collectDoc(docID));
    }
}
