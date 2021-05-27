package com.wu.studyniuke.controller;

import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.service.DiscussPostService;
import com.wu.studyniuke.service.UserService;
import com.wu.studyniuke.util.CommunityUtil;
import com.wu.studyniuke.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author me
 * @create 2021-05-26-20:30
 */
@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

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
    public String showDiscussPost(@PathVariable("discussId") int discussId, Model model){
        DiscussPost post = discussPostService.queryDiscussPost(discussId);
        model.addAttribute("post",post);
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user",user);
        return "site/discuss-detail";

    }

}
