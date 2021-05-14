package com.wu.studyniuke;

import com.wu.studyniuke.dao.DiscussPostMapper;
import com.wu.studyniuke.dao.UserMapper;

import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

/**
 * @author me
 * @create 2021-05-12-23:26
 */

@SpringBootTest
@ContextConfiguration(classes = StudyniukeApplication.class)
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectUserById(1);
        System.out.println(user);

        user = userMapper.selectUserByName("system");
        System.out.println(user);

        user = userMapper.selectUserByEmail(null);
        System.out.println(user);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setId(5);
        user.setUsername("老王");
        user.setPassword("123456");
        user.setEmail("123@qq.com");
        user.setCreateTime(new Date());
        user.setSalt("happy");
        user.setHeaderUrl("jav.com");

        int rows = userMapper.insertUser(user);
        System.out.println(rows);

        System.out.println(user.getId());


    }

    @Test
    public void testSelectDiscussPort(){
        int i = discussPostMapper.selectDiscussPostRows(123123);
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0,0,10);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost.toString()+"查询的discussPost");
        }
        System.out.println(i);

    }

}
