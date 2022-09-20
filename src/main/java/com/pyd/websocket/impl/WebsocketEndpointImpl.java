package com.pyd.websocket.impl;

import com.pyd.websocket.WebSocketBean;
import com.pyd.websocket.WebsocketEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ Program: insight
 * @ Description:
 * @ Author: fei tao
 * @ Create: 2022-04-29 10:18
 */
@Service
@ServerEndpoint(value = "/web/ws/{projectId}/{userId}")
@Component // 关键点1
public class WebsocketEndpointImpl implements WebsocketEndpoint {

    private static Logger log = LoggerFactory.getLogger(WebsocketEndpointImpl.class);
    /**
     * 错误最大重试次数
     */
    private static final int MAX_ERROR_NUM = 3;

    /**
     * 用来存放每个客户端对应的webSocket对象。
     */
    private static Map<String, Map<String, WebSocketBean>> webSocketInfo;
    private static Map<String,WebSocketBean> concurrentHashMap;

    static {
        // concurrent包的线程安全map
        webSocketInfo = new ConcurrentHashMap<>();
        concurrentHashMap = new ConcurrentHashMap();
    }

    private static WebsocketEndpointImpl webSocket; // 关键点2

    @PostConstruct  // 关键点3
    public void init(){
        webSocket = this;
    }


    @OnOpen
    public void onOpen(Session session, EndpointConfig config, @PathParam("userId") String userId,@PathParam("projectId") String projectId) {
        WebSocketBean bean = new WebSocketBean();
        bean.setSession(session);
        concurrentHashMap.put(userId,bean);
        webSocketInfo.put(projectId, concurrentHashMap);
        // 更新用户状态
//        webSocket.userService.updateUserStatusById(userId, true); // 关键点4
//        log.info("ws项目:"+projectId+"，客户端连接服务器userId :" + userId + "，当前连接数：" + countUser(projectId));
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId,@PathParam("projectId") String projectId) {
        // 客户端断开连接移除websocket对象
        Map<String,WebSocketBean> concurrentHashMap = webSocketInfo.get(projectId);
        if(concurrentHashMap != null){
            concurrentHashMap.remove(userId);
            // 更新用户状态
//            webSocket.userService.updateUserStatusById(userId, false);
        }
//        log.info("ws项目:"+projectId+"，客户端 userId: " + userId + " 断开连接，当前连接数：" + countUser(projectId));

    }

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("userId") String userId,@PathParam("projectId") String projectId) {
        if(message.equals("ping"))  sendMessage(session,"pong",userId,projectId);
//        log.info("ws项目:"+projectId+"，客户端 userId: " + userId + "，消息:" + message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
//        log.error("ws发生错误" + throwable.getMessage(), throwable);
    }

    public void sendMessage(Session session, String message, String projectId, String userId) {

//        log.info("ws项目:"+projectId+"，用户:"+userId+"，发送消息:" + message);
        try {
            // 发送消息
            synchronized (session) {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            }
            // 清空错误计数
            this.cleanErrorNum(projectId, userId);
        } catch (Exception e) {
//            log.error("ws项目:"+projectId+"，用户:"+userId+"，发送消息失败" + e.getMessage(), e);
            int errorNum = this.getErroerLinkCount(projectId, userId);

            // 小于最大重试次数重发
            if (errorNum <= MAX_ERROR_NUM) {
                sendMessage(session, message, projectId, userId);
            } else {
                log.error("ws发送消息失败超过最大次数");
                // 清空错误计数
                this.cleanErrorNum(projectId, userId);
            }
        }
    }

    @Override
    public void batchSendMessage(String projectId,String message) {
        Map<String,WebSocketBean> concurrentHashMap = webSocketInfo.get(projectId);
        if(concurrentHashMap != null){
            Set<Map.Entry<String, WebSocketBean>> set = concurrentHashMap.entrySet();
            for(Map.Entry<String, WebSocketBean> map: set ){
                sendMessage(map.getValue().getSession(), message,projectId, map.getKey());
            }
        }
    }

    @Override
    public void sendMessageById(String projectId,String userId, String message) {
        Map<String,WebSocketBean> concurrentHashMap = webSocketInfo.get(projectId);
        if(concurrentHashMap != null){
            WebSocketBean webSocketBean = concurrentHashMap.get(userId);
            if (webSocketBean != null) {
                sendMessage(webSocketBean.getSession(), message, projectId,userId);
            }
        }
    }

    /**
     * 清空错误计数
     */
    private void cleanErrorNum(String projectId, String userId){
        Map<String,WebSocketBean> concurrentHashMap = webSocketInfo.get(projectId);
        if(concurrentHashMap != null){
            WebSocketBean webSocketBean = concurrentHashMap.get(userId);
            if (webSocketBean != null) {
                webSocketBean.cleanErrorNum();
            }
        }
    }

    /**
     * 获取错误计数
     */
    private int getErroerLinkCount(String projectId, String userId){
        int errorNum = 0;
        Map<String,WebSocketBean> concurrentHashMap = webSocketInfo.get(projectId);
        if(concurrentHashMap != null){
            WebSocketBean webSocketBean = concurrentHashMap.get(userId);
            if (webSocketBean != null) {
                errorNum = webSocketBean.getErroerLinkCount();
            }
        }
        return errorNum;
    }

    private Integer countUser (String projectId){
        int size = 0;
        Map<String,WebSocketBean> concurrentHashMap = webSocketInfo.get(projectId);
        if(concurrentHashMap != null) {
            size = concurrentHashMap.size();
        }
        return size;
    }

}
