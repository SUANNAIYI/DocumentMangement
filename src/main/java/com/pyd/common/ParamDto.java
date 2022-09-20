package com.pyd.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ParamDto implements Serializable {
    private String name;
    private Long id;
    private String userIds;
    private String docIds;
    // 当前页面
    private int currentPage;
    // 数据条数
    private int pageSize;
    // 关键词
    private String texts;
    // 上传者
    private String uploader;
    // 发送者
    private String sender;
    // 文件种类
    private String type;
    // 时间区间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private List<LocalDateTime> time;
}
