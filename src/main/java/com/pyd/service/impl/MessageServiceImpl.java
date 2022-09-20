package com.pyd.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pyd.entity.Message;
import com.pyd.mapper.MessageMapper;
import com.pyd.service.MessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pyd.util.ShiroUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    MessageMapper messageMapper;

    @Override
    public Message newMsgRec(Long userID, String title, String content, LocalDateTime dateTime, String flag) {
        Message message = new Message();
        message.setUserID(userID);
        message.setTitle(title);
        message.setContent(content);
        message.setDate(dateTime);
        message.setFlag(flag);
        messageMapper.insert(message);
        return message;
    }

    @Override
    public String deleteMsgRec(Long userID) {
        QueryWrapper<Message> messageQueryWrapper = new QueryWrapper<>();
        messageQueryWrapper.in("userID", ShiroUtil.getProfile().getId());
        messageMapper.delete(messageQueryWrapper);
        return "删除成功";
    }

    @Override
    public List<Message> showMsg(Long userID) {
        QueryWrapper<Message> messageQueryWrapper = new QueryWrapper<>();
        messageQueryWrapper.eq("userID", userID);
        return messageMapper.selectList(messageQueryWrapper);
    }

    @Override
    public Boolean readMsg(Long msgID) {
        Message message = messageMapper.selectById(msgID);
        message.setFlag("true");
        messageMapper.updateById(message);
        return true;
    }
}
