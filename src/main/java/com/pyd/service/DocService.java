package com.pyd.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pyd.entity.Doc;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pyd.entity.Record;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;


public interface DocService extends IService<Doc> {
    String deleteFileRec(Long id);

    IPage<Doc> getFolderDocs(int currentPage, int pageSize, Long folderId);

    IPage<Doc> searchDocs(int currentPage, int pageSize, String texts);

    String[] previewDoc(Long id);

    Record getRec(Long docID, Long folderID, Long userID);

    Doc newDocRec(String docName, String type, String uploader, LocalDateTime time, String content, String path, Long folder, String sender);

    String[] editDoc(Long id);

    IPage<Doc> detailSearch(int currentPage, int pageSize, String uploader, String sender, String type, List<LocalDateTime> dateTimes);

    HashMap<String, Integer> saveFile(HttpServletRequest request , HttpServletResponse response, String id, String userName, String st) throws IOException;

    String previewVerDoc(String id);

    Long getRootID(Long id);

    Long searchFolder(String folderName, Long createrID);
}
