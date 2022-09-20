package com.pyd.service.impl;

import com.pyd.entity.Record;
import com.pyd.mapper.RecordMapper;
import com.pyd.service.RecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

    @Autowired
    RecordMapper recordMapper;

    @Override
    public Record recordStatus(Long folderID, Long docID, Long userID, String status) {
        Record record = new Record();
        record.setFolderID(folderID);
        record.setDocID(docID);
        record.setUserID(userID);
        record.setStatus(status);
        recordMapper.insert(record);
        return record;
    }
}
