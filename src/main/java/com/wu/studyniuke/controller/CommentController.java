package com.wu.studyniuke.controller;

import com.wu.studyniuke.entity.Comment;
import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.service.CommentService;
import com.wu.studyniuke.util.HostHolder;
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
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") String discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);

        commentService.addComment(comment);
        return "redirect:/discuss/detail/"+ discussPostId;
    }
}
