package com.wu.studyniuke.controller;

import com.wu.studyniuke.entity.Page;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.service.FollowService;
import com.wu.studyniuke.service.UserService;
import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.CommunityUtil;
import com.wu.studyniuke.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author me
 * @create 2021-07-17-10:50
 */

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityId,int entityType){
        User user = hostHolder.getUser();

        followService.follow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"关注成功");
    }

    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityId,int entityType){
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"取消关注成功");
    }

    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable(name = "userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("用户为空");
        }

        model.addAttribute("user",user);

        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        page.setPath("/followees/"+userId);
        page.setLimit(5);


        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());


        if(followees!=null){
            for (Map<String, Object> followee : followees) {
                User user1 = (User) followee.get("user");
                followee.put("hasFollowed",hasFollowed(user1.getId()));
            }
        }

        model.addAttribute("users",followees);
        return "site/followee";
    }

    private boolean hasFollowed(int userId){
        if(hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
    }

    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable(name = "userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("用户为空");
        }

        model.addAttribute("user",user);

        page.setRows((int) followService.findFollowerCount(userId, ENTITY_TYPE_USER));
        page.setPath("/followers/"+userId);
        page.setLimit(5);


        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());

        if(followers!=null){
            for (Map<String, Object> follower : followers) {
                User user1 = (User) follower.get("user");
                follower.put("hasFollowed",hasFollowed(user1.getId()));
            }
        }

        model.addAttribute("users",followers);

        return "site/follower";
    }
}
