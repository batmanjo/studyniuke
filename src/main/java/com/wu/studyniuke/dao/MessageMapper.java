package com.wu.studyniuke.dao;

import com.wu.studyniuke.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author me
 * @create 2021-05-30-16:32
 */
@Mapper
public interface MessageMapper {

    List<Message> selectConversations(int userId,int offset,int limit);

    int selectConversationsCount(int userId);

    List<Message> selectLetters(String conversationId,int offset,int limit);

    int selectLetterCount(String conversationId);

    int selectLetterUnreadCount(int userId,String conversationId);

    int insertMessage(Message message);

    int updateStatus(List<Integer> ids,int status);

    List<Message> selectSystemNotices(int useId);
}
