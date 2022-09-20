package com.pyd.service;

import com.pyd.entity.Version;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDateTime;
import java.util.List;


public interface VersionService extends IService<Version> {
    Version newVersionRec(Long docID, String docName, String editor, LocalDateTime time, String path);
    String deleteVersionRec(String ids);
    List<Version> showVersion(Long id);
}
