package com.wu.studyniuke.controller;

import com.wu.studyniuke.entity.Comment;
import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.entity.Page;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.service.CommentService;
import com.wu.studyniuke.service.DiscussPostService;
import com.wu.studyniuke.service.UserService;
import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.CommunityUtil;
import com.wu.studyniuke.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author me
 * @create 2021-05-26-20:30
 */
@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user =hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"unLogin");
        }
        if(title==null||content==null){
            return CommunityUtil.getJSONString(403,"不能为空");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPost.setUserId(user.getId());

        discussPostService.addDiscussPost(discussPost);
        return CommunityUtil.getJSONString(0,"success");

    }

    @RequestMapping(path="/detail/{discussId}",method = RequestMethod.GET)
    public String showDiscussPost(@PathVariable("discussId") int discussId, Model model, Page page){
        DiscussPost post = discussPostService.queryDiscussPost(discussId);
        model.addAttribute("post",post);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);

        page.setPath("/discuss/detail/"+discussId);
        page.setRows(post.getCommentCount());
        page.setLimit(5);
        List<Comment> comments =
                commentService.queryCommentByEntity(ENTITY_TYPE_POST, discussId, page.getOffset(), page.getLimit());

        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if(comments!=null){
            for (Comment comment : comments) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));


                // 回复列表
                List<Comment> replyList = commentService.queryCommentByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = commentService.queryCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }


        model.addAttribute("comments", commentVoList);
        return "site/discuss-detail";

    }

}
