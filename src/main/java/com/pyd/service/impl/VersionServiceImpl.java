package com.pyd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pyd.entity.Version;
import com.pyd.mapper.VersionMapper;
import com.pyd.service.VersionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pyd.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class VersionServiceImpl extends ServiceImpl<VersionMapper, Version> implements VersionService {

    @Autowired
    VersionMapper versionMapper;

    @Override
    public Version newVersionRec(Long docID, String docName, String editor, LocalDateTime time, String path) {
        Version version = new Version();
        version.setDocID(docID);
        version.setEditor(editor);
        version.setTime(time);
        version.setPath(path);
        version.setDocName(docName);
        versionMapper.insert(version);
        return version;
    }

    @Override
    public String deleteVersionRec(String ids) {
        List<String> versions = Arrays.asList(ids.split(","));
        QueryWrapper<Version> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", versions);
        List<Version> versionList = versionMapper.selectList(queryWrapper);
        for (Version version : versionList){
            File file = new File(FileUtil.basepath2 + version.getPath());
            file.delete();
        }
        versionMapper.delete(queryWrapper);
        return "删除成功";
    }

    @Override
    public List<Version> showVersion(Long id) {
        QueryWrapper<Version> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("docID", id);
        return versionMapper.selectList(queryWrapper);
    }
}
