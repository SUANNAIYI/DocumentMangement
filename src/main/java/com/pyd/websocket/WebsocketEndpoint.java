package com.pyd.websocket;

public interface WebsocketEndpoint {
    /**
     * 向所有在线用户群发消息
     *
     * @param projectId 项目ID
     * @param message   发送给客户端的消息
     */
    void batchSendMessage(String projectId, String message);

    /**
     * 发送给对应的用户
     *
     * @param userId    用户的ID
     * @param projectId 项目ID
     * @param message   发送的消息
     */
    void sendMessageById(String projectId, String userId, String message);
}
