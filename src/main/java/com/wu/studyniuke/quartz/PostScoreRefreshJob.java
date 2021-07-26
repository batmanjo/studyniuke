package com.wu.studyniuke.quartz;

import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.service.DiscussPostService;
import com.wu.studyniuke.service.ElasticSearchService;
import com.wu.studyniuke.service.LikeService;
import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticSearchService elasticsearchService;

    private static final Date EPOCH;

    static {
        try {
            EPOCH = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-06-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败！", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() == 0) {
            LOGGER.info("任务取消，没有需要刷新的帖子！");
            return;
        }
        LOGGER.info("[任务开始] 正在刷新帖子分数：" + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        LOGGER.info("[任务结束] 帖子分数刷新完毕！");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.queryDiscussPost(postId);

        if (post == null) {
            LOGGER.error("该帖子不存在：id = " + postId);
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == STATUS_ESSENCE;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score =
                Math.log10(Math.max(w, 1))
                        + (post.getCreateTime().getTime() - EPOCH.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
