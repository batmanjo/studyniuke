package com.wu.studyniuke.controller;

import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.entity.Page;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.service.DiscussPortService;
import com.wu.studyniuke.service.UserService;
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
public class HomeController {

    @Autowired
    private DiscussPortService discussPortService;
    @Autowired
    private UserService userService;

    @RequestMapping(path = "/index" , method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page){
        page.setRows(discussPortService.selectDiscussPostRows(0));
        page.setPath("/index");


        List<DiscussPost> list = discussPortService.selectDiscussPosts(0, page.getOffset(), page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list != null ){
            for (DiscussPost discussPost : list) {
                Map<String,Object> map  = new HashMap<>();

                User user = userService.findUserById(discussPost.getUserId());
                map.put("user",user);
                map.put("post",discussPost);
                discussPosts.add(map);
            }
        }


//        HashMap<String, Object> map2 = new HashMap<>();
//        User user1 = new User();
//        user1.setUsername("张三");
//        map2.put("user1",user1);
//        model.addAttribute("hello",map2);
//        System.out.println(map2.get("user1"));
          System.out.println(discussPosts);


        model.addAttribute("discussPosts",discussPosts);
        return "/index2";
    }
}
