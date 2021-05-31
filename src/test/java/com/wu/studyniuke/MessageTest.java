package com.wu.studyniuke;

import com.wu.studyniuke.dao.MessageMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author me
 * @create 2021-05-30-17:37
 */
@SpringBootTest
@ContextConfiguration(classes = StudyniukeApplication.class)
public class MessageTest {
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testMessage(){
        System.out.println(messageMapper.selectConversations(2,0,5));
        System.out.println(messageMapper.selectConversationsCount(2));
        System.out.println(messageMapper.selectLetters("2_3",0,5));
        System.out.println(messageMapper.selectLetterCount("2_3"));
        System.out.println(messageMapper.selectLetterUnreadCount(2,"2_3"));
    }
}
