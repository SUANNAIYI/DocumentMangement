package com.pyd.websocket;

/**
 * @ Program: insight
 * @ Description: 按用户推送
 * @ Author: fei tao
 * @ Create: 2022-04-29 10:16
 */
public class SendMsg extends SendMsgAll {
    /**
     * 用户ID
     */
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}