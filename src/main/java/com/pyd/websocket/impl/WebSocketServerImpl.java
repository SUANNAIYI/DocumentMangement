package com.pyd.websocket.impl;

import com.alibaba.fastjson.JSON;
import com.pyd.websocket.SendMsg;
import com.pyd.websocket.SendMsgAll;
import com.pyd.websocket.WebsocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @ Program: insight
 * @ Description:
 * @ Author: fei tao
 * @ Create: 2022-04-29 10:26
 */
@Service
public class WebSocketServerImpl implements WebsocketService {
    @Autowired
    RedisTemplate<String,String> redisTemplate;

    @Override
    public void sendMessageAll(String projectId , Map<String, Object> data) {
        SendMsgAll sendMsgAll = new SendMsgAll();
        sendMsgAll.setProjectId(projectId);
        sendMsgAll.setData(data);
        redisTemplate.convertAndSend("mh-topic", JSON.toJSONString(sendMsgAll));

    }

    @Override
    public void sendMessageById(String projectId,String userId, Map<String, Object> data) {
        SendMsg sendMsg = new SendMsg();
        sendMsg.setProjectId(projectId);
        sendMsg.setUserId(userId);
        sendMsg.setData(data);
        redisTemplate.convertAndSend("ptp-topic", JSON.toJSONString(sendMsg));
    }

    @Override
    public void sendMessageById(String projectId, String userId, String msg) {
        SendMsg sendMsg = new SendMsg();
        sendMsg.setProjectId(projectId);
        sendMsg.setUserId(userId);
        sendMsg.setMsg(msg);
        redisTemplate.convertAndSend("ptp-topic", JSON.toJSONString(sendMsg));
    }
}