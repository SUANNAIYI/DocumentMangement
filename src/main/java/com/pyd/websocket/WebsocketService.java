package com.pyd.websocket;

import java.util.Map;

public interface WebsocketService {
    /**
     * 向所有在线用户群发消息
     * @param data 发送给客户端的消息
     */
    void sendMessageAll(String projectId, Map<String, Object> data);

    /**
     * 发送给对应的用户
     * @param userId 用户的ID
     * @param data 发送的消息
     */
    void sendMessageById(String projectId, String userId, Map<String, Object> data);

    /**
     * 发送给对应的用户
     * @param userId 用户的ID
     * @param msg 发送的消息
     */
    void sendMessageById(String projectId, String userId, String msg);
}
