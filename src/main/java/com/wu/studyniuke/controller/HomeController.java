package com.wu.studyniuke.controller;

import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.entity.Page;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.service.DiscussPostService;
import com.wu.studyniuke.service.LikeService;
import com.wu.studyniuke.service.MessageService;
import com.wu.studyniuke.service.UserService;
import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.HostHolder;
import org.apache.ibatis.annotations.AutomapConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author me
 * @create 2021-05-13-15:26
 */
@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        page.setRows(discussPostService.selectDiscussPostRows(0));
        page.setPath("/index");


        List<DiscussPost> list = discussPostService.selectDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost discussPost : list) {
                Map<String, Object> map = new HashMap<>();

                User user = userService.findUserById(discussPost.getUserId());
                map.put("user", user);
                map.put("post", discussPost);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPost.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);

            }
        }

        if(hostHolder.getUser() != null){
            int noticeUnreadCounts = messageService.findNoticeUnreadCount(hostHolder.getUser().getId(), null);
            int letterUnreadCount  = messageService.findLetterUnreadCount(hostHolder.getUser().getId(), null);
            int count = noticeUnreadCounts+letterUnreadCount;

            model.addAttribute("allUnreadCount", count);
        }

        model.addAttribute("discussPosts", discussPosts);

        return "/index";
    }

    @RequestMapping(path = "/errorPage")
    public String getErrorPage() {
        return "/error/500";
    }

    @RequestMapping(path = "/denied",method = RequestMethod.GET)
    public String getDenied(){
        return "error/404";
    }
}
