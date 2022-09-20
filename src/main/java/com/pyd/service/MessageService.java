package com.pyd.service;

import com.pyd.entity.Message;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageService extends IService<Message> {
    Message newMsgRec(Long userID, String title, String content, LocalDateTime dateTime, String flag);
    String deleteMsgRec(Long userID);
    List<Message> showMsg(Long userID);
    Boolean readMsg(Long msgID);
}
