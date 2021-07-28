package com.wu.studyniuke.service;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wu.studyniuke.dao.DiscussPostMapper;
import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.util.CommunityUtil;
import com.wu.studyniuke.util.SensitiveFilter;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author me
 * @create 2021-05-13-11:18
 */
@Service
public class DiscussPostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.maxsize}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    //帖子总数缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(
                        key -> {
                            if (key == null || key.length() == 0) {
                                throw new IllegalArgumentException("参数错误!");
                            }
                            String[] params = key.split(":");
                            if (params == null || params.length != 2) {
                                throw new IllegalArgumentException("参数错误!");
                            }
                            int offset = Integer.valueOf(params[0]);
                            int limit = Integer.valueOf(params[1]);
                            // 二级缓存：Redis -> mysql
                            LOGGER.debug("load post list from DB");
                            return discussPostMapper.selectDiscussPosts(
                                    0, offset, limit, 1);
                        });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(
                        key -> {
                            LOGGER.debug("load post list from DB");
                            return discussPostMapper.selectDiscussPostRows(key);
                        });
    }

    public List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode) {
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        LOGGER.debug("load post list from Database");

        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }

    public int selectDiscussPostRows(@Param("userId") int userId) {
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        LOGGER.debug("load post list from DB");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) {
            throw new IllegalArgumentException("参数不能为空");

        }
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }

    public DiscussPost queryDiscussPost(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int discussPostId, int type) {
        return discussPostMapper.updateType(discussPostId, type);
    }

    public int updateStatus(int discussPostId, int status) {
        return discussPostMapper.updateStatus(discussPostId, status);
    }

    public int updateScore(int postId, double score) {
        return discussPostMapper.updateScore(postId, score);
    }

}
