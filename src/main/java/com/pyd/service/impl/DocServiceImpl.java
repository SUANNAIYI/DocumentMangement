package com.pyd.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pyd.entity.*;
import com.pyd.mapper.*;
import com.pyd.service.DocService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pyd.service.RecordService;
import com.pyd.service.TransferService;
import com.pyd.service.VersionService;
import com.pyd.util.ReadUtils;
import com.pyd.util.ShiroUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.shiro.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static com.pyd.service.impl.FolderServiceImpl.*;
import static com.pyd.util.FileUtil.basepath2;


@Service
public class DocServiceImpl extends ServiceImpl<DocMapper, Doc> implements DocService {
    @Autowired
    RecordService recordService;
    @Autowired
    VersionService versionService;
    @Autowired
    TransferService transferService;
    @Autowired
    DocMapper docMapper;
    @Autowired
    FolderMapper folderMapper;
    @Autowired
    RecordMapper recordMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    TransferMapper transferMapper;
    @Autowired
    VersionMapper versionMapper;

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

    // 获取当前用户可见的文件夹列表
    private List<Folder> getFolderList() {
        List<Folder> folderList = new ArrayList<>();
        for(Long rootFolderId : new Long[]{publicFolder, personFolder}){
            Folder folder = folderMapper.selectById(rootFolderId);
            folderList.add(folder);
            getSubFolder(folder, folderList);
        }
        return folderList;
    }

    //获得文件夹根目录
    @Override
    public Long getRootID(Long id){
        Long rootId = id;
        //获得根文件夹
        while (true){
            Folder folder = folderMapper.selectById(rootId);
            if (folder != null){
                if (folder.getParent() != null){
                    rootId = folder.getParent();
                }else {
                    break;
                }
            }else {
                break;
            }
        }
        return rootId;
    }


    // 搜索文件夹
    @Override
    public Long searchFolder(String folderName, Long createrID){
        QueryWrapper<Folder> folderQueryWrapper = new QueryWrapper<>();
        Map<String, Object> map = new HashMap<>();
        map.put("foldername", folderName);
        map.put("createrID", createrID);
        folderQueryWrapper.allEq(map);
        return folderMapper.selectOne(folderQueryWrapper).getId();
    }

    //通过文件id获取Record中的status
    @Override
    public Record getRec(Long docID, Long folderID, Long userID){
        QueryWrapper<Record> recordQueryWrapper = new QueryWrapper<>();
        Map<String, Object> map = new HashMap<>();
        map.put("docID", docID);
        map.put("folderID", folderID);
        map.put("userID", userID);
        recordQueryWrapper.allEq(map);
        return recordMapper.selectOne(recordQueryWrapper);
    }

    //删除本地文件
    private void deleteFile(Doc doc){
        File file = new File(basepath2 + doc.getPath());
        file.delete();
    }

    //删除文件记录
    @Override
    public String deleteFileRec(Long id){
        Doc doc = docMapper.selectById(id);
        Record record = getRec(doc.getId(), doc.getFolder(), ShiroUtil.getProfile().getId());
        QueryWrapper<Version> versionQueryWrapper = new QueryWrapper<>();
        // 删除版本记录
        versionQueryWrapper.eq("docID", id).select("id");
        versionMapper.delete(versionQueryWrapper);
        // 删除文件状态
        if (record != null){
            recordMapper.deleteById(record.getId());
        }
        // 搜索本地文件地址
        List<Doc> docList = docMapper.selectList(new QueryWrapper<Doc>().eq("path", doc.getPath()).select("path"));
        // 没有文件使用此本地地址是则删除本地文件
        if (docList.size() == 1){
            deleteFile(doc);
        }
        // 删除发布文件则删除所有流转信息
        if (doc.getFolder() == releaseFolder){
            Long firstID = transferService.getFirstID(id);
            transferMapper.delete(new QueryWrapper<Transfer>().eq("firstID", firstID));
        }
        docMapper.deleteById(id);
        return "删除成功";
    }

    //设置文件操作
    private void setDocStatus(Doc doc, Long rootId){
        String[] operations; // 定义支持的操作
        Record record = getRec(doc.getId(), doc.getFolder(), ShiroUtil.getProfile().getId());
        if (record == null){
            doc.setStatus("未读");
        }else {
            doc.setStatus(record.getStatus());
        }
        // 个人文件夹
        if (rootId.equals(personFolder)){
            // 发送文件夹仅支持预览
            Long sendFolder = searchFolder("发送文件", ShiroUtil.getProfile().getId());
            if (doc.getFolder() == sendFolder){
                operations = new String[]{"预览"};
                doc.setAction(operations);
            }
            // 其他个人文件夹支持所有操作
            else {
                operations = new String[]{"预览","编辑","发送","审核","共享","发布","版本记录"};
                doc.setAction(operations);
            }
        }
        // 公共文件夹
        else {
            // 发布文件夹仅支持预览
            if (doc.getFolder() == releaseFolder) {
                operations = new String[]{"预览"};
                doc.setAction(operations);
            }
            else {
                operations = new String[]{"预览","发送"};
                doc.setAction(operations);
            }
        }
    }


    // 新建文件记录
    @Override
    public Doc newDocRec(String docName, String type, String uploader, LocalDateTime time, String content, String path, Long folder, String sender) {
        Doc doc = new Doc();
        doc.setDocname(docName);
        doc.setType(type);
        doc.setUploader(uploader);
        doc.setUploadtime(time);
        doc.setContent(content);
        doc.setPath(path);
        doc.setFolder(folder);
        doc.setSender(sender);
        docMapper.insert(doc);
        return doc;
    }

    // 预览文件
    @Override
    public String[] previewDoc(Long id) {
        Doc doc = docMapper.selectById(id);
        String[] res = new String[3];
        res[0] = doc.getPath();
        res[1] = doc.getDocname();
        res[2] = "false";
        Long folderID = doc.getFolder();
        //更新record表中的status
        QueryWrapper<Record> recordQueryWrapper = new QueryWrapper<>();
        Map<String, Object> map = new HashMap<>();
        map.put("docID", doc.getId());
        map.put("folderID", doc.getFolder());
        map.put("userID", ShiroUtil.getProfile().getId());
        recordQueryWrapper.allEq(map);
        Record record = recordMapper.selectOne(recordQueryWrapper);
        if (record == null){
            recordService.recordStatus(folderID, id,ShiroUtil.getProfile().getId(), "已阅");
        }else {
            if (record.getStatus().equals("待审核")){
                record.setStatus("已审核");
            }
            recordMapper.updateById(record);
        }
        Long firstID = transferService.getFirstID(doc.getId());  // 获取原文件ID
        // 增加文件流转记录
        transferService.newTransferRec(firstID, doc.getId(), "预览文件", ShiroUtil.getProfile().getId(), LocalDateTime.now());
        return res;
    }

    // 编辑文件
    @Override
    public String[] editDoc(Long id) {
        Doc doc = docMapper.selectById(id);
        String[] res = new String[4];
        res[0] = doc.getPath();
        res[1] = doc.getDocname();
        res[2] = doc.getType().substring(doc.getType().lastIndexOf(".") + 1);
        res[3] = DigestUtils.md5Hex(LocalDateTime.now().toString() + id + Math.random()*1000);
        String type = doc.getType();
        if (type.equals(".docx") || type.equals(".txt") || type.equals(".doc") || type.equals(".pptx") || type.equals(".xlsx")){
            return res;
        }else {
            return new String[]{"文件类型不支持"};
        }
    }

    // 从url下载文件
    private Boolean downloadFIleFromUrl(String url, String path){
        try{
            URL httpUrl = new URL(url);
            File file = new File(path);
            FileUtils.copyURLToFile(httpUrl, file);
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    // onlyoffice回调接口
    @Override
    public HashMap<String, Integer> saveFile(HttpServletRequest request, HttpServletResponse response, String id, String userName, String st){
        HashMap data = JSON.parseObject(st, HashMap.class);
        String status = data.get("status").toString();
        Doc doc = docMapper.selectById(id);
        List<Doc> sendDocs = null;
        // 查找发送文件，并同步
        if (doc.getSender() != null){
            // 搜索其他相同发送文件
            QueryWrapper<Doc> docQueryWrapper = new QueryWrapper<>();
            HashMap<String, Object> map = new HashMap<>();
            map.put("docname", doc.getDocname());
            map.put("path", doc.getPath());
            docQueryWrapper.allEq(map);
            sendDocs = docMapper.selectList(docQueryWrapper);
        }
        // 编辑者身份信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("username", userName);
        Long userID = userMapper.selectOne(userQueryWrapper).getId();
        String path = basepath2 + doc.getPath();
        // 随机生成新文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "").toLowerCase() + doc.getType();
        String newPath = path.substring(0, path.lastIndexOf('/')) + '/' + newFileName;
        String dbPath = newPath.substring(newPath.lastIndexOf("static"));
        if ((status.equals("2")) || status.equals("3") || status.equals("6")){
            String downloadURL = data.get("url").toString();
            if (downloadURL != null){
                if(downloadFIleFromUrl(downloadURL, newPath)){
                    doc.setPath(dbPath);
                    docMapper.updateById(doc);
                    // 更新发送文件本地地址
                    if (sendDocs != null){
                        for (Doc tempDoc : sendDocs){
                            tempDoc.setPath(dbPath);
                            docMapper.updateById(tempDoc);
                        }
                    }
                    // 更新record表中的status
                    Record record = getRec(doc.getId(), doc.getFolder(), userID);
                    if (record == null){
                        recordService.recordStatus(doc.getFolder(), Long.parseLong(id), userID, "编辑");
                    }else {
                        // 待审核状态
                        if (record.getStatus().equals("待审核")){
                            record.setStatus("已审核");
                            recordMapper.updateById(record);
                        }else {
                            if (record.getStatus().equals("已阅")){
                                record.setStatus("编辑");
                                recordMapper.updateById(record);
                            }
                        }
                    }
                    // 添加版本记录
                    versionService.newVersionRec(doc.getId(), doc.getDocname(), userName, LocalDateTime.now(), dbPath);
                    // 更新content字段
                    if (doc.getType().equals(".txt") || doc.getType().equals(".docx") || doc.getType().equals(".doc")){
                        String text = ReadUtils.getText(basepath2 + doc.getPath());
                        doc.setContent(text);
                        docMapper.updateById(doc);
                        // 更新发送文件内容
                        if (sendDocs != null){
                            for (Doc tempDoc : sendDocs){
                                tempDoc.setContent(text);
                                docMapper.updateById(tempDoc);
                            }
                        }
                    }
                    Long firstID = transferService.getFirstID(doc.getId());  // 获取原文件ID
                    // 增加文件流转记录
                    transferService.newTransferRec(firstID, doc.getId(), "编辑文件", userID, LocalDateTime.now());
                    System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "更新成功");
                }else{
                    System.out.println("更新失败");
                }
            }
        }
        response.setContentType(String.valueOf(MediaType.APPLICATION_JSON));
        response.setStatus(200);
        HashMap<String, Integer> rsp = new HashMap<>();
        rsp.put("error", 0);
        return rsp;
    }

    // 预览版本记录文件
    @Override
    public String previewVerDoc(String id) {
        return versionMapper.selectById(id).getPath();
    }

    // 获取当前文件夹下文件
    @Override
    public IPage<Doc> getFolderDocs(int currentPage, int pageSize, Long folderId) {
        IPage page = new Page(currentPage,pageSize);
        //获得根文件夹
        Long rootId = getRootID(folderId);
        QueryWrapper<Doc> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("folder", folderId).orderByDesc("id");
        IPage docIPage = docMapper.selectPage(page, queryWrapper);
        List<Doc> docList = docIPage.getRecords();
        for (Doc doc : docList){
            setDocStatus(doc, rootId);  // 设置文件操作
        }
        docIPage.setRecords(docList);
        return docIPage;
    }

    // 关键词检索
    @Override
    public IPage<Doc> searchDocs(int currentPage, int pageSize, String texts) {
        IPage page = new Page(currentPage,pageSize);
        List<Folder> folderList = getFolderList();  // 获取文件夹列表
        List<Long> folderIds = new ArrayList<>();  // 获取文件夹id
        for (Folder folder : folderList){
            folderIds.add(folder.getId());
        }
        QueryWrapper<Doc> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("folder", folderIds)
                .and(docQueryWrapper -> docQueryWrapper.like("docname", texts).or().like("content", texts)).orderByDesc("id");
        IPage docIPage = docMapper.selectPage(page, queryWrapper);
        List<Doc> docList = docIPage.getRecords();
        for (Doc doc : docList){
            setDocStatus(doc, getRootID(doc.getFolder()));  // 设置文件状态
        }
        System.out.println(docIPage.getTotal());
        docIPage.setRecords(docList);
        return docIPage;
    }

    // 高级检索
    @Override
    public IPage<Doc> detailSearch(int currentPage, int pageSize, String uploader, String sender, String type, List<LocalDateTime> dateTimes) {
        IPage page = new Page(currentPage,pageSize);
        List<Folder> folderList = getFolderList();  // 获取文件夹列表
        List<Long> folderIds = new ArrayList<>();
        // 获取文件夹id
        for (Folder folder : folderList){
            folderIds.add(folder.getId());
        }
        boolean flag = false;
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (dateTimes != null && dateTimes.size() == 2){
            start = dateTimes.get(0);
            end = dateTimes.get(1);
            flag = true;
        }
        LocalDateTime finalStart = start;
        LocalDateTime finalEnd = end;
        QueryWrapper<Doc> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("folder", folderIds)
                .and(StringUtils.hasText(uploader), docQueryWrapper -> docQueryWrapper.eq("uploader", uploader))
                .and(StringUtils.hasText(sender), docQueryWrapper -> docQueryWrapper.eq("sender", sender))
                .and(StringUtils.hasText(type), docQueryWrapper -> docQueryWrapper.eq("type", type))
                .and(flag, docQueryWrapper -> docQueryWrapper.between("uploadtime", finalStart, finalEnd)).orderByDesc("id");
        IPage docIPage = docMapper.selectPage(page, queryWrapper);
        List<Doc> docList = docIPage.getRecords();
        for (Doc doc : docList){
            setDocStatus(doc, getRootID(doc.getFolder()));  // 设置文件操作
        }
        System.out.println(docIPage.getTotal());
        docIPage.setRecords(docList);
        return docIPage;
    }
}