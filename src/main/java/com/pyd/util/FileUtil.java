package com.pyd.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pyd.entity.Folder;
import com.pyd.mapper.FolderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class FileUtil {
    public static final String basepath;  // D:\IdeaProjects\Document\src\main\resources\static
    public static final String basepath2;  // D:\IdeaProjects\Document\src\main\resources\

    static {
        try {
            basepath = new File("src/main/resources/static/").getCanonicalPath();
            basepath2 = basepath.substring(0, basepath.lastIndexOf("static"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static Boolean CreateFolder(String path){
        File folder = new File(path);
        if (!folder.exists()){
            folder.mkdirs();
            return true;
        }else {
            return false;
        }
    }
}
