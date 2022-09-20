package com.pyd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pyd.entity.Doc;
import com.pyd.entity.Folder;
import com.pyd.mapper.DocMapper;
import com.pyd.mapper.FolderMapper;
import com.pyd.mapper.UserMapper;
import com.pyd.service.DocService;
import com.pyd.service.FolderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pyd.util.FileUtil;
import com.pyd.util.ShiroUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder> implements FolderService {
    @Autowired
    FolderMapper folderMapper;

    @Autowired
    DocMapper docMapper;

    @Autowired
    DocService docService;

    @Autowired
    UserMapper userMapper;

    public static Long publicFolder;  // 公共文件夹ID
    public static Long personFolder;  // 个人文件夹ID
    public static Long releaseFolder;  // 发布文件夹ID

    private void newBaseFolder(){
        // 创建用户收藏夹
        Folder Favorites = folderMapper.selectOne(new QueryWrapper<Folder>().eq("createrID", ShiroUtil.getProfile().getId())
                .and(folderQueryWrapper -> folderQueryWrapper.eq("foldername", "收藏夹")));
        if (Favorites == null){
            newFolder("收藏夹", personFolder);
        }
        // 创建用户接收文件
        Folder receiveFolder = folderMapper.selectOne(new QueryWrapper<Folder>().eq("createrID", ShiroUtil.getProfile().getId())
                .and(folderQueryWrapper -> folderQueryWrapper.eq("foldername", "接收文件")));
        if (receiveFolder == null){
            newFolder("接收文件", personFolder);
        }
        // 创建用户发送文件
        Folder sendFolder = folderMapper.selectOne(new QueryWrapper<Folder>().eq("createrID", ShiroUtil.getProfile().getId())
                .and(folderQueryWrapper -> folderQueryWrapper.eq("foldername", "发送文件")));
        if (sendFolder == null){
            newFolder("发送文件", personFolder);
        }
    }

    //基本文件夹初始化
    @Override
    public void init() {
        //新建资源池、个人文件与发布文件夹
        String[] folderNames = new String[]{"资源池", "个人文件", "发布文件", "收藏夹"};
        QueryWrapper<Folder> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("foldername", folderNames);
        List<Folder> folders = folderMapper.selectList(queryWrapper);
        if (folders.isEmpty()){
            publicFolder = newFolder("资源池", null).getId();  // 创建公共文件夹本地文件
            personFolder = newFolder("个人文件", null).getId();  // 创建个人文件夹本地文件
            releaseFolder = newFolder("发布文件", publicFolder).getId();  // 创建发布文件夹本地文件
        }else {
            publicFolder = folders.get(0).getId();  // 获取公共文件夹ID
            personFolder = folders.get(1).getId();  // 获取个人文件夹ID
            releaseFolder = folders.get(2).getId();  // 获取发布文件夹ID
        }
        newBaseFolder();  //个人发布文件夹与接收文件夹初始化
    }

    //通过ids获取文件夹全路径
    @Override
    public String getAllPath(Long id){
        StringBuilder path = new StringBuilder(); // 全路径
        Deque<String> tempPath = new LinkedList<>();
        while (true){
            Folder folder = folderMapper.selectById(id);
            if(folder != null){
                if(folder.getParent() != null){
                    tempPath.offerFirst(folder.getId() + "/");  // 加入全路径首部
                    id = folder.getParent();  // 转换文件夹
                }else{
                    tempPath.offerFirst(folder.getId() + "/");
                    break;
                }
            }else {
                break;
            }
        }
        while (!tempPath.isEmpty()){
            path.append(tempPath.pollFirst());
        }
        return path.toString();
    }


    //重命名文件夹
    @Override
    public Folder renameFolder(String name, Long id) {
        Folder folder = folderMapper.selectById(id);
        folder.setFoldername(name);  // 更新文件名
        folderMapper.updateById(folder);
        return folder;
    }

    //删除文件夹及其子文件
    @Override
    public String deleteFolder(Long id) {
        Folder folder = folderMapper.selectById(id);  // 获取文件夹信息
        QueryWrapper<Doc> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("folder", folder.getId());
        List<Doc> docList = docMapper.selectList(queryWrapper);  // 文件夹下文件列表
        //删除文件夹下的文件记录
        for (Doc doc : docList){
            docService.deleteFileRec(doc.getId());
        }
        File file = new File(FileUtil.basepath + "/" + getAllPath(id));
        deleteFile(file);  // 删除本地文件夹及其子文件
        folderMapper.deleteById(id);
        return "删除成功！";
    }

    // 删除本地文件夹及其子文件
    private void deleteFile(File file) {
        File[] files = file.listFiles();  // 获取目录下子文件
        //遍历该目录下的文件对象
        for (File f : files) {
            if (f.isDirectory()) {
                deleteFile(f);
            } else {
                f.delete();
            }
        }
        file.delete();
    }

    // 新建文件夹
    @Override
    public Folder newFolder(String name, Long id) {
        String path = getAllPath(id);  // 获取父文件夹全路径
        Folder folder = new Folder();
        if(path.equals("")){
            folder.setParent(null);  // 根文件夹
        }else {
            folder.setParent(id);
        }
        folder.setFoldername(name);
        folder.setCreatetime(LocalDateTime.now());
        folder.setCreaterID(ShiroUtil.getProfile().getId());
        folderMapper.insert(folder);  // 存入数据库
        FileUtil.CreateFolder(FileUtil.basepath +  "/" + path  + "/" + folder.getId());// 创建本地文件
        return folder;
    }

    // 获取子文件夹列表
    private List<Folder> getSubFolder(Folder folder){
        QueryWrapper<Folder> queryWrapper = new QueryWrapper<>();
        List<Folder> subfolders;
        if (folder.getId().equals(personFolder)){
            Map<String, Object> map = new HashMap<>();
            //用户创建文件夹
            map.put("parent", folder.getId());
            map.put("createrID", ShiroUtil.getProfile().getId());
            queryWrapper.allEq(map);
            subfolders = folderMapper.selectList(queryWrapper);
        }else {
            queryWrapper.eq("parent", folder.getId());
            subfolders = folderMapper.selectList(queryWrapper);
        }
        if(subfolders.size() > 0) {
            for (Folder subfolder : subfolders) {
                subfolder.setChildren(getSubFolder(subfolder));//递归查找
            }
            return subfolders;
        }else {
            return null;
        }
    }

    // 获取文件夹列表
    @Override
    public List<Folder> getFolderList() {
        init();
        List<Folder> folders = new ArrayList<>();
        for(Long rootFolderId : new Long[]{publicFolder, personFolder}){
            Folder folder = folderMapper.selectById(rootFolderId);
            folder.setChildren(getSubFolder(folder));
            folders.add(folder);
        }
        return folders;
    }
}
