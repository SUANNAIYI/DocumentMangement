package com.pyd.websocket;

import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import javax.annotation.Resource;

/**
 * @ Program: insight
 * @ Description:
 * @ Author: fei tao
 * @ Create: 2022-04-29 10:23
 */
@Component
public class RedisReceiver {
    @Resource
    WebsocketEndpoint websocketEndpoint;

    /**
     * 处理一对一消息
     * @param message 消息队列中的消息
     */
    public void sendMsg(String message) {
        SendMsg msg = JSONObject.parseObject(message, SendMsg.class);
        websocketEndpoint.sendMessageById(msg.getProjectId(),msg.getUserId(),msg.getMsg());
    }

    /**
     * 处理广播消息
     * @param message
     */
    public void sendAllMsg(String message){
        SendMsgAll msg = JSONObject.parseObject(message, SendMsgAll.class);
        websocketEndpoint.batchSendMessage(msg.getProjectId(),msg.getMsg());
    }
}