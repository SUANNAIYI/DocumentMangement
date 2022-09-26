package com.pyd.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pyd.entity.*;
import com.pyd.mapper.DocMapper;
import com.pyd.mapper.FolderMapper;
import com.pyd.mapper.RecordMapper;
import com.pyd.mapper.UserMapper;
import com.pyd.service.*;
import com.pyd.util.ReadUtils;
import com.pyd.util.ShiroUtil;
import com.pyd.websocket.WebsocketService;
import org.apache.commons.io.FileUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.pyd.util.FileUtil.*;
import static com.pyd.service.impl.FolderServiceImpl.*;

@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private fileEntity fileEntity;

    @Autowired
    FolderMapper folderMapper;

    @Autowired
    RecordMapper recordMapper;

    @Autowired
    DocMapper docMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    DocService docService;

    @Autowired
    VersionService versionService;

    @Autowired
    RecordService recordService;

    @Autowired
    MessageService messageService;

    @Autowired
    FolderService folderService;

    @Autowired
    WebsocketService websocketService;

    @Autowired
    TransferService transferService;


    // 上传文件
    @Override
    public String uploadFile(MultipartFile file, Long id) throws IOException {
        System.out.println(file.getContentType());
        // 判断文件类型是否正确
        if (!fileEntity.getAllowTypes().contains(file.getContentType())){
            return "上传文件类型不支持！";
        }
        String path = folderService.getAllPath(id);  // 文件夹全地址
        String docType = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".")).toLowerCase();  // 获取文件类型
        File tempFile = new File(basepath + "/" + path + UUID.randomUUID().toString().replace("-", "").toLowerCase() + docType);
        file.transferTo(tempFile);
        String relativePath = "static/" + path + tempFile.getName();  // 数据库中存的相对地址
        // 产生文件记录
        Doc tempDoc = docService.newDocRec(file.getOriginalFilename(), docType, ShiroUtil.getProfile().getUsername(),
                LocalDateTime.now(), ReadUtils.getText(tempFile.getPath()), relativePath, id, null);
        // 上传至个人文件夹下产生文件版本记录
        if (docService.getRootID(id).equals(personFolder)){
            versionService.newVersionRec(tempDoc.getId(), tempDoc.getDocname(), ShiroUtil.getProfile().getUsername(),
                    LocalDateTime.now(), tempDoc.getPath());
        }
        //生成上传文件流转记录
        transferService.newTransferRec(tempDoc.getId(), tempDoc.getId(), "上传文件", ShiroUtil.getProfile().getId(), LocalDateTime.now());
        return "上传成功";
    }

    //下载文件
    @Override
    public ResponseEntity<byte[]> downloadFileByIds(String id, HttpServletResponse response){
        List<String> ids = Arrays.asList(id.split(","));
        QueryWrapper<Doc> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<Doc> docList = docMapper.selectList(queryWrapper);
        if (docList.size() == 1){
                InputStream in;
                ServletOutputStream outputStream;
                try {
                    Doc doc = docList.get(0);
                    in = new FileInputStream(basepath2 + doc.getPath());
                    String docname = doc.getDocname();
                    String docName = URLEncoder.encode(docname,"UTF-8");
                    response.setHeader("content-type","application/force-download");
                    response.setHeader("Content-Disposition","attachment;filename=" + docName);
                    outputStream = response.getOutputStream();
                    byte[] buffer=new byte[10240];
                    int len;
                    while((len=in.read(buffer))!=-1){
                        outputStream.write(buffer,0,len);
                    }
                    outputStream.flush();
                    outputStream.close();
                    in.close();
                    return ResponseEntity.ok().body(buffer);
                } catch (FileNotFoundException e) {
                    System.out.println("文件未找到");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("原文件不存在".getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("下载失败".getBytes());
                }
            }
        else {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
            try {
                for (Doc doc : docList) {
                    zipOutputStream.putNextEntry(new ZipEntry(doc.getDocname()));
                    String filePath = doc.getPath();
                    File file = new File(basepath2 + "/" + filePath);
                    zipOutputStream.write(FileUtils.readFileToByteArray(file));
                }
                zipOutputStream.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                String title = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
                String zipFileName = title + ".zip";
                zipFileName = URLEncoder.encode(zipFileName, "UTF-8");
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                response.setHeader("Content-disposition", "attachment;filename=" + zipFileName);
                response.flushBuffer();
                return ResponseEntity.ok().body(byteArrayOutputStream.toByteArray());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("下载失败".getBytes());
            }
        }
    }

    //发送文件
    @Override
    public String sendFiles(String userIds, String docId) throws IOException {
        Doc doc = docMapper.selectById(Long.parseLong(docId));  // 查询文件
        List<String> userId = Arrays.asList(userIds.split(","));  // 多用户列表
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.in("id", userId);
        List<User> userList = userMapper.selectList(userWrapper);
        File rawFile = new File(basepath2 + doc.getPath());  // 原文件
        String newFileName = UUID.randomUUID().toString().replace("-", "").toLowerCase();  // 随机生成新文件名
        String docType = rawFile.getName().substring(rawFile.getName().lastIndexOf(".")).toLowerCase();  // 获取文件类型
        Long sendFolder = docService.searchFolder("发送文件", ShiroUtil.getProfile().getId()); //文件副本地址
        String path = "static/" + personFolder + "/" + sendFolder + "/" + newFileName + docType; // 数据库中存的相对地址
        File newFile= new File(basepath2 + path);
        FileUtils.copyFile(rawFile, newFile);  // 复制本地文件
        StringBuilder receiveUserNames = new StringBuilder();  // 接收人姓名
        for (User user : userList) {
            receiveUserNames.append(user.getUsername());
            if (!user.equals(userList.get(userList.size() - 1))) {
                receiveUserNames.append("、");
            }
        }
        // 不仅仅发送给自己则发送文件夹产生记录
        Doc sendDoc = docService.newDocRec(doc.getDocname(), doc.getType(), doc.getUploader(),
                LocalDateTime.now(), doc.getContent(), path, sendFolder, null);
        Long first = transferService.getFirstID(doc.getId());  // 原文件ID
        // 增加发送文件文件流转过程
        transferService.newTransferRec(first, sendDoc.getId(), "发送给" + receiveUserNames, ShiroUtil.getProfile().getId(), LocalDateTime.now());
        // 接收用户接收文件夹产生记录
        for (User user : userList){
            Long receiveFolderID = docService.searchFolder("接收文件", user.getId());  // 接收文件夹ID
            // 添加接收文件记录
            Doc tempDoc = docService.newDocRec(doc.getDocname(), doc.getType(), doc.getUploader(),
                    LocalDateTime.now(), doc.getContent(), path, receiveFolderID, ShiroUtil.getProfile().getUsername());
            Long firstID = transferService.getFirstID(doc.getId());  // 原文件ID
            // 增加发送文件文件流转过程
            transferService.newTransferRec(firstID, tempDoc.getId(), "接收文件", user.getId(), LocalDateTime.now());
            // 添加版本记录
            versionService.newVersionRec(tempDoc.getId(), doc.getDocname(), ShiroUtil.getProfile().getUsername(),
                    LocalDateTime.now(), tempDoc.getPath());
            // WebSocket发送消息
            Map<String, Object> message = new HashMap<>();
            String title = doc.getDocname();
            String content = ShiroUtil.getProfile().getUsername() + "给您发送了一份文件";
            LocalDateTime date = LocalDateTime.now();
            message.put("title", title);
            message.put("content", content);
            message.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            message.put("readFlag", false);
            websocketService.sendMessageById("projectId", user.getUsername(), JSON.toJSONString(message));
            messageService.newMsgRec(user.getId(), title, content, date, "false");
        }
        return "发送成功";
    }

    // 发布文件
    @Override
    public String releaseFiles(Long id) throws IOException {
        Doc doc = docMapper.selectById(id);
        String path = basepath2 + doc.getPath();
        String newPath = basepath + "/" + publicFolder+ "/" + releaseFolder + "/";  // 发布文件地址
        File rawFile = new File(path);
        String docType = rawFile.getName().substring(rawFile.getName().lastIndexOf(".")).toLowerCase();
        String newName = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        File newFile = new File(newPath + newName + docType);
        FileUtils.copyFile(rawFile, newFile);  // 复制文件至发布文件夹下
        // 新增文件记录
        Doc tempDoc = docService.newDocRec(doc.getDocname(), doc.getType(), doc.getUploader(), LocalDateTime.now(),
                doc.getContent(), "static/" + publicFolder + "/" + releaseFolder + "/" + newName + docType, releaseFolder, null);
        Long firstID = transferService.getFirstID(doc.getId());  // 获取原文件ID
        transferService.newTransferRec(firstID, tempDoc.getId(), "已发布", ShiroUtil.getProfile().getId(), LocalDateTime.now());  // 增加文件流转记录
        docService.deleteFileRec(id);  // 删除记录
        return newPath;
    }

    // 审核文件
    @Override
    public String reviewDoc(Long docID, Long userID) {
        Doc doc = docMapper.selectById(docID);  // 获取文件信息
        String userName = userMapper.selectById(userID).getUsername();  // 获取接收文件用户信息
        Long receiveFolderID = docService.searchFolder("接收文件", userID);  // 查询接收者接收文件夹ID
        // 接收者增加接收文件状态
        Doc tempDoc = docService.newDocRec(doc.getDocname(), doc.getType(), doc.getUploader(),
                LocalDateTime.now(), doc.getContent(), doc.getPath(), receiveFolderID, ShiroUtil.getProfile().getUsername());
        Long firstID = transferService.getFirstID(doc.getId());  // 获取原文件ID
        // 增加文件流转记录
        transferService.newTransferRec(firstID, tempDoc.getId(), "请求"+ userName +"审核", ShiroUtil.getProfile().getId(), LocalDateTime.now());
        recordService.recordStatus(receiveFolderID, tempDoc.getId(), userID, "待审核");  // 新增待审核文件记录
        // 新增版本记录
        versionService.newVersionRec(tempDoc.getId(), doc.getDocname(), ShiroUtil.getProfile().getUsername(), LocalDateTime.now(), doc.getPath());
        // WebSocket发送消息
        Map<String, Object> message = new HashMap<>();
        String title = doc.getDocname();
        String content = ShiroUtil.getProfile().getUsername() + "请您审核一份文件";
        LocalDateTime date = LocalDateTime.now();
        message.put("title", title);
        message.put("content", content);
        message.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        message.put("readFlag", false);
        websocketService.sendMessageById("projectId", userName, JSON.toJSONString(message));
        messageService.newMsgRec(userID, title, content, date, "false");
        docService.deleteFileRec(docID);
        return "送审成功";
    }

    // 共享文件
    @Override
    public String shareDoc(Long id) throws IOException{
        Doc doc = docMapper.selectById(id);
        String path = basepath2 + doc.getPath();
        String newPath = basepath + "/" + publicFolder + "/";  // 资源池目录地址
        File rawFile = new File(path);
        String docType = rawFile.getName().substring(rawFile.getName().lastIndexOf(".")).toLowerCase();
        String newName = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        File newFile = new File(newPath + newName + docType);
        FileUtils.copyFile(rawFile, newFile);  // 复制文件
        // 新增文件记录
        Doc tempDoc = docService.newDocRec(doc.getDocname(), doc.getType(), doc.getUploader(),
                LocalDateTime.now(), doc.getContent(), path, publicFolder, null);
        Long firstID = transferService.getFirstID(doc.getId());  // 获取原文件ID
        // 增加文件流转记录
        transferService.newTransferRec(firstID, tempDoc.getId(), "共享至资源池", ShiroUtil.getProfile().getId(), LocalDateTime.now());
        return newPath;
    }

    // 退回文件
    @Override
    public String backDocs(Doc doc, String type){
        if (doc.getSender() == null){
            return "该文件不可退回！";  // 接收文件才可退回
        }else {
            Long userID = userMapper.selectOne(new QueryWrapper<User>().eq("username",
                    doc.getSender()).select("id")).getId();  // 接收者ID
            Long receiveFolder = docService.searchFolder("接收文件", userID);  // 接收者接收文件夹
            // 新增接收文件记录
            Doc receiveDoc = docService.newDocRec(doc.getDocname(), doc.getType(), doc.getUploader(),
                    LocalDateTime.now(), doc.getContent(), doc.getPath(), receiveFolder, ShiroUtil.getProfile().getUsername());
            String content;  // WebSocket消息内容
            if (type.equals("不通过")){
                // 获取原文件ID
                Long firstID = transferService.getFirstID(doc.getId());
                // 增加文件流转记录
                transferService.newTransferRec(firstID, receiveDoc.getId(), "审核未通过", ShiroUtil.getProfile().getId(), LocalDateTime.now());
                recordService.recordStatus(receiveFolder, receiveDoc.getId(), userID, "审核未通过");  // 新增退回文件状态
                content = ShiroUtil.getProfile().getUsername() + "给您退回了一份审核未通过文件";
            }else {
                // 获取原文件ID
                Long firstID = transferService.getFirstID(doc.getId());
                // 增加文件流转记录
                transferService.newTransferRec(firstID, receiveDoc.getId(), "审核通过", ShiroUtil.getProfile().getId(), LocalDateTime.now());
                recordService.recordStatus(receiveFolder, receiveDoc.getId(), userID, "审核通过");  // 新增待发布文件状态
                content = ShiroUtil.getProfile().getUsername() + "给您发送了一份审核通过文件";
            }
            // WebSocket发送消息
            Map<String, Object> message = new HashMap<>();
            String title = doc.getDocname();
            LocalDateTime date = LocalDateTime.now();
            message.put("title", title);
            message.put("content", content);
            message.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            message.put("readFlag", false);
            websocketService.sendMessageById("projectId", doc.getSender(), JSON.toJSONString(message));
            messageService.newMsgRec(userID, title, content, date, "false");  //记录消息
            // 添加版本记录
            versionService.newVersionRec(receiveDoc.getId(), receiveDoc.getDocname(), ShiroUtil.getProfile().getUsername(),
                    LocalDateTime.now(), receiveDoc.getPath());
            docService.deleteFileRec(doc.getId());
            return "退回成功";
        }
    }

    // 收藏文件
    @Override
    public String collectDoc(Long docID) throws IOException{
        Long favorites = docService.searchFolder("收藏夹", ShiroUtil.getProfile().getId());
        Doc doc = docMapper.selectById(docID);
        File rawFile = new File(basepath2 + doc.getPath());  // 原文件
        String newFileName = UUID.randomUUID().toString().replace("-", "").toLowerCase();  // 随机生成新文件名
        String docType = rawFile.getName().substring(rawFile.getName().lastIndexOf(".")).toLowerCase();  // 获取文件类型
        String path = "static/" + personFolder + "/" + favorites + "/" + newFileName + docType; // 新文件相对地址
        // 新增接收文件记录
        Doc tempDoc = docService.newDocRec(doc.getDocname(), doc.getType(), doc.getUploader(),
                LocalDateTime.now(), doc.getContent(), path, favorites, null);
        File newFile = new File(basepath2 + path);
        FileUtils.copyFile(rawFile, newFile);  // 复制文件
        // 新增版本记录
        versionService.newVersionRec(tempDoc.getId(), doc.getDocname(), ShiroUtil.getProfile().getUsername(), LocalDateTime.now(), doc.getPath());
//        Long firstID = transferService.getFirstID(docID);
        // 增加文件流转信息
        transferService.newTransferRec(tempDoc.getId(), tempDoc.getId(), "收藏文件", ShiroUtil.getProfile().getId(), LocalDateTime.now());  // 增加文件流转记录
        return "收藏成功";
    }
}
