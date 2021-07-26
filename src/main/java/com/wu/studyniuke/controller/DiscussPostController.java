package com.wu.studyniuke.controller;

import com.wu.studyniuke.entity.*;
import com.wu.studyniuke.event.EventProducer;
import com.wu.studyniuke.service.CommentService;
import com.wu.studyniuke.service.DiscussPostService;
import com.wu.studyniuke.service.LikeService;
import com.wu.studyniuke.service.UserService;
import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.CommunityUtil;
import com.wu.studyniuke.util.HostHolder;
import com.wu.studyniuke.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "unLogin");
        }
        if (title == null || content == null) {
            return CommunityUtil.getJSONString(403, "不能为空");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setCreateTime(new Date());
        discussPost.setUserId(user.getId());

        discussPostService.addDiscussPost(discussPost);

        // 触发发帖事件
        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(user.getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(discussPost.getId());

        eventProducer.fireEvent(event);

        //计算新发帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,discussPost.getId());

        return CommunityUtil.getJSONString(0, "success");

    }

    @RequestMapping(path = "/detail/{discussId}", method = RequestMethod.GET)
    public String showDiscussPost(@PathVariable("discussId") int discussId, Model model, Page page) {
        //帖子
        DiscussPost post = discussPostService.queryDiscussPost(discussId);
        model.addAttribute("post", post);
        //帖子的作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //点赞
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussId);
        model.addAttribute("likeCount", likeCount);

        int likeStatus = hostHolder.getUser() == null ?
                0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussId);
        model.addAttribute("likeStatus", likeStatus);


        //评论分页显示
        page.setPath("/discuss/detail/" + discussId);
        page.setRows(post.getCommentCount());
        page.setLimit(5);
        List<Comment> comments =
                commentService.queryCommentByEntity(ENTITY_TYPE_POST, discussId, page.getOffset(), page.getLimit());

        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

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

                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

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

    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int discussPostId){
        discussPostService.updateType(discussPostId,TYPE_TOP);

        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(discussPostId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/untop",method = RequestMethod.POST)
    @ResponseBody
    public String setUnTop(int discussPostId){
        discussPostService.updateType(discussPostId,TYPE_COMMON);

        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(discussPostId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int discussPostId){
        discussPostService.updateStatus(discussPostId,STATUS_ESSENCE);

        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(discussPostId);
        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,discussPostId);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/unwonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setUnWonderful(int discussPostId){
        discussPostService.updateStatus(discussPostId,STATUS_COMMON);

        Event event = new Event();
        event.setTopic(TOPIC_PUBLISH);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(discussPostId);
        eventProducer.fireEvent(event);

        //计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,discussPostId);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int discussPostId){
        discussPostService.updateStatus(discussPostId,STATUS_BLOCK);

        Event event = new Event();
        event.setTopic(TOPIC_DELETE);
        event.setUserId(hostHolder.getUser().getId());
        event.setEntityType(ENTITY_TYPE_POST);
        event.setEntityId(discussPostId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }


}
