package com.pyd.websocket;

import java.util.Map;

/**
 * @ Program: insight
 * @ Description: 推送全部
 * @ Author: fei tao
 * @ Create: 2022-04-29 10:12
 */
public class SendMsgAll {
    /**
     * websocket业务数据(json)
     */
    private String msg;

    private Map<String, Object> data;
    /**
     * 业务模块类型
     */
    private String type;

    /**
     * 项目ID
     */
    private String projectId;


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}