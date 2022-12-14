package com.pyd.websocket;

import javax.websocket.Session;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ Program: insight
 * @ Description: 存储WebSocket Session
 * @ Author: fei tao
 * @ Create: 2022-04-29 09:49
 */
public class WebSocketBean {
    /**
     * 连接session对象
     */
    private Session session;

    /**
     * 连接错误次数
     */
    private AtomicInteger erroerLinkCount = new AtomicInteger(0);

    public int getErroerLinkCount() {
        // 线程安全,以原子方式将当前值加1，注意：这里返回的是自增前的值
        return erroerLinkCount.getAndIncrement();
    }

    public void cleanErrorNum() {
        // 清空计数
        erroerLinkCount = new AtomicInteger(0);
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}