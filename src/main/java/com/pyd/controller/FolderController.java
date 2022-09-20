package com.pyd.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pyd.common.Result;
import com.pyd.common.ParamDto;
import com.pyd.entity.Folder;
import com.pyd.mapper.FolderMapper;
import com.pyd.service.DocService;
import com.pyd.service.FolderService;
import com.pyd.util.ShiroUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.pyd.service.impl.FolderServiceImpl.*;


@Api(tags = "文件目录管理模块")
@RestController
@RequestMapping("/folder")
public class FolderController {

    @Autowired
    FolderService folderService;
    @Autowired
    FolderMapper folderMapper;
    @Autowired
    DocService docService;

    private List<Folder> getSubFolder(Folder folder, List<Folder> folderList){
        QueryWrapper<Folder> queryWrapper = new QueryWrapper<>();
        List<Folder> subfolders;
        if (folder.getId().equals(personFolder)){
            Map<String, Object> map = new HashMap<>();
            // 用户个人文件夹
            map.put("parent", folder.getId());
            map.put("createrID", ShiroUtil.getProfile().getId());
            queryWrapper.allEq(map);
            subfolders = folderMapper.selectList(queryWrapper);
            folderList.addAll(subfolders);
        }else {
            queryWrapper.eq("parent", folder.getId());
            subfolders = folderMapper.selectList(queryWrapper);
            folderList.addAll(subfolders);
        }
        if(subfolders.size() > 0) {
            for (Folder subfolder : subfolders) {
                List<Folder> tempList = getSubFolder(subfolder, folderList);  // 递归查找
                if (tempList != null){
                    if (!new HashSet<>(tempList).containsAll(tempList)){
                        folderList.addAll(tempList);
                    }
                }
            }
            return subfolders;
        }else {
            return null;
        }
    }

    private List<Folder> getFolderList() {
        List<Folder> folderList = new ArrayList<>();
        Folder folder = folderMapper.selectById(personFolder);
        folderList.add(folder);
        getSubFolder(folder, folderList);
        return folderList;
    }

    @ApiOperation(value = "获取文件夹目录")
    @GetMapping("/search")
    public Result getFolderInf(){
        return Result.succ(folderService.getFolderList());
    }

    @ApiOperation(value = "新建文件夹")
    @PostMapping("/create")
    public Result createFolder(@RequestBody ParamDto paramDto){
        Long id = paramDto.getId();
        String folderName = paramDto.getName();
        Long receiveFolder = docService.searchFolder("接收文件", ShiroUtil.getProfile().getId());  // 接收文件夹ID
        Long sendFolder = docService.searchFolder("发送文件", ShiroUtil.getProfile().getId());  // 发送文件夹ID
        // 收发、发布文件夹下不允许创建文件夹
        if (id.equals(receiveFolder) || id.equals(sendFolder) || id.equals(releaseFolder)){
            return Result.fail("该目录下不允许创建文件夹!");
        }else {
            List<Folder> folders = getFolderList();  // 获取个人文件夹下的所有文件夹
            List<String> folderNames = new ArrayList<>();
            for (Folder folder : folders){
                folderNames.add(folder.getFoldername());  // 获取个人文件夹下所有文件夹名
            }
            // 文件夹名判重
            if (folderNames.contains(folderName)){
                return Result.fail("目录名已存在!");
            }else {
                return Result.succ(folderService.newFolder(folderName, id));
            }
        }
    }

    @ApiOperation(value = "重命名文件夹")
    @PostMapping("/rename")
    public Result renameFolder(@RequestBody ParamDto paramDto){
        Long id = paramDto.getId();
        Long receiveFolder = docService.searchFolder("接收文件", ShiroUtil.getProfile().getId());
        Long sendFolder = docService.searchFolder("发送文件", ShiroUtil.getProfile().getId());
        Long favorites = docService.searchFolder("收藏夹", ShiroUtil.getProfile().getId());
        // 基本文件夹不允许重命名
        if (id.equals(receiveFolder) || id.equals(sendFolder) || id.equals(favorites) || id.equals(releaseFolder) ||
                id.equals(publicFolder) || id.equals(personFolder)){
            return Result.fail("该目录不允许重命名!");
        }else {
            List<Folder> folders = getFolderList();  // 获取个人文件夹下的所有文件夹
            List<String> folderNames = new ArrayList<>();
            for (Folder folder : folders){
                folderNames.add(folder.getFoldername());  // 获取个人文件夹下所有文件夹名
            }
            // 文件夹名判重
            if (folderNames.contains(paramDto.getName())){
                return Result.fail("目录名已存在!");
            }else {
                return Result.succ(folderService.renameFolder(paramDto.getName(), id));
            }
        }
    }

    @ApiOperation(value = "删除文件夹")
    @PostMapping("/delete")
    public Result deleteFolder(@RequestBody ParamDto paramDto){
        Long id = paramDto.getId();
        Long receiveFolder = docService.searchFolder("接收文件", ShiroUtil.getProfile().getId());
        Long sendFolder = docService.searchFolder("发送文件", ShiroUtil.getProfile().getId());
        Long favorites = docService.searchFolder("收藏夹", ShiroUtil.getProfile().getId());
        // 基本文件夹不允许删除
        if (id.equals(receiveFolder) || id.equals(sendFolder) || id.equals(favorites) || id.equals(releaseFolder) ||
                id.equals(publicFolder) || id.equals(personFolder)){
            return Result.fail("该目录不允许删除!");
        }else {
            return Result.succ(folderService.deleteFolder(paramDto.getId()));
        }
    }
}
