package com.wu.studyniuke.service;

import com.wu.studyniuke.dao.MessageMapper;
import com.wu.studyniuke.entity.Message;
import com.wu.studyniuke.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author me
 * @create 2021-05-30-17:43
 */
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<Message> findConversations(int userId,int offset,int limit){
        return messageMapper.selectConversations(userId,offset,limit);
    }

    public int findConversationCount(int userId){
        return messageMapper.selectConversationsCount(userId);
    }

    public List<Message> findLetters(String conversationId,int offset,int limit){
        return messageMapper.selectLetters(conversationId,offset,limit);
    }

    public int findLetterCounts(String conversationId){
        return messageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId){
        return messageMapper.selectLetterUnreadCount(userId,conversationId);
    }

    public int addMessage(Message message){
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));

        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> ids){
        return messageMapper.updateStatus(ids,1);
    }

    public List<Message> findSystemNotices(int userId,String conversationId,int offset,int limit){
        return messageMapper.selectSystemNotices(userId,conversationId,offset,limit);
    }

    public int findNoticeUnreadCount(int userId,String conversationId){
        return messageMapper.selectNoticeUnreadCount(userId,conversationId);
    }

    public int findNoticeCounts(int userId,String conversationId){
        return messageMapper.selectNoticeCounts(userId,conversationId);
    }
}

