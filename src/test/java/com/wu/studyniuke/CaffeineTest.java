package com.wu.studyniuke;

import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * @author me
 * @create 2021-07-27-21:43
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = StudyniukeApplication.class)
public class CaffeineTest {

    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void testInsert(){
        for (int i = 0; i < 30; i++) {
            DiscussPost discussPost = new DiscussPost();

            discussPost.setUserId(10);
            discussPost.setTitle("沈阳森林");
            discussPost.setContent("因为你，我会记得那一分钟");
            discussPost.setCreateTime(new Date());
            discussPost.setScore(Math.random()*2000);
            discussPostService.addDiscussPost(discussPost);
        }
    }

    @Test
    public void testCache(){
        System.out.println(discussPostService.selectDiscussPosts(0,0,10,1));
        System.out.println(discussPostService.selectDiscussPosts(0,0,10,1));
        System.out.println(discussPostService.selectDiscussPosts(0,0,10,1));
        System.out.println(discussPostService.selectDiscussPosts(0,0,10,0));
    }
}
