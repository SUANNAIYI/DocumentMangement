package com.pyd.service;

import com.pyd.common.ParamDto;
import com.pyd.common.Result;
import com.pyd.entity.Doc;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public interface UploadService {
    String uploadFile(MultipartFile file, Long path) throws Exception;
    ResponseEntity<byte[]> downloadFileByIds(String id, HttpServletResponse response) throws IOException;
    String sendFiles(String userIds, String docIds)throws IOException;
    String releaseFiles(Long id) throws IOException;
    String reviewDoc(Long docID, Long userID);
    String shareDoc(Long id) throws IOException;
    String backDocs(String ids, String type) throws IOException;
    String collectDoc(Long docID) throws IOException;

}
