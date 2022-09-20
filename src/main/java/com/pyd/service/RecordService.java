package com.pyd.service;

import com.pyd.entity.Record;
import com.baomidou.mybatisplus.extension.service.IService;


public interface RecordService extends IService<Record> {
    Record recordStatus(Long folderID, Long docID, Long userID, String status);
}
