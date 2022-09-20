package com.pyd.service;

import com.pyd.entity.Folder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface FolderService extends IService<Folder> {

    Folder newFolder(String name, Long id);

    List<Folder> getFolderList();

    String getAllPath(Long id);

    Folder renameFolder(String name, Long id);

    String deleteFolder(Long id);

    void init();
}
