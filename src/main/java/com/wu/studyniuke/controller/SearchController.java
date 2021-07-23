package com.wu.studyniuke.controller;

import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.entity.Page;
import com.wu.studyniuke.service.ElasticSearchService;
import com.wu.studyniuke.service.LikeService;
import com.wu.studyniuke.service.UserService;
import com.wu.studyniuke.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author me
 * @create 2021-07-23-21:48
 */
@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticSearchService elasticsearchService;

    @Autowired private UserService userService;

    @Autowired private LikeService likeService;

    /**
     * 搜索帖子
     *
     * @param keyword
     * @param page
     * @param model
     * @return
     */
    @GetMapping("/search")
    public String search(String keyword, Page page, Model model) {
        // 搜索帖子
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(
                        keyword, page.getCurrent() - 1, page.getLimit());
        // 聚合数据
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>(16);
                // 帖子
                map.put("post", post);
                // 作者
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put(
                        "likeCount",
                        likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());
        return "site/search";
    }
}

