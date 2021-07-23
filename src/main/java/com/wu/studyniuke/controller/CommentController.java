package com.wu.studyniuke.controller;

import com.wu.studyniuke.entity.Comment;
import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.entity.Event;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.event.EventProducer;
import com.wu.studyniuke.service.CommentService;
import com.wu.studyniuke.service.DiscussPostService;
import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.HostHolder;
import com.wu.studyniuke.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @author me
 * @create 2021-05-30-15:17
 */
@RequestMapping("/comment")
@Controller
public class CommentController implements CommunityConstant {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);

        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event();
        event.setUserId(hostHolder.getUser().getId());
        event.setTopic(TOPIC_COMMENT);
        event.setEntityType(comment.getEntityType());
        event.setEntityId(comment.getEntityId());
        event.setData("postId", discussPostId);

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event.setEntityUserId(discussPostService.queryDiscussPost(comment.getEntityId()).getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            event.setEntityUserId(commentService.queryCommentById(comment.getEntityId()).getUserId());
        }

        eventProducer.fireEvent(event);

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 触发发帖事件
            event = new Event();
            event.setTopic(TOPIC_PUBLISH);
            event.setUserId(comment.getUserId());
            event.setEntityType(ENTITY_TYPE_POST);
            event.setEntityId(discussPostId);
            eventProducer.fireEvent(event);

        }

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
