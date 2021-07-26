package com.wu.studyniuke.controller;

import com.wu.studyniuke.entity.Event;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.event.EventProducer;
import com.wu.studyniuke.service.LikeService;
import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.CommunityUtil;
import com.wu.studyniuke.util.HostHolder;
import com.wu.studyniuke.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

/**
 * @author me
 * @create 2021-07-03-20:53
 */
@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;


    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();

        //注意拦截空用户

        likeService.like(user.getId(), entityType, entityId, entityUserId);

        long likeCount = likeService.findEntityLikeCount(entityType, entityId);

        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        if (likeStatus == 1) {
            Event event = new Event();
            event.setTopic(TOPIC_LIKE);
            event.setUserId(user.getId());
            event.setEntityType(entityType);
            event.setEntityId(entityId);
            event.setEntityUserId(entityUserId);
            event.setData("PostId", postId);

            eventProducer.fireEvent(event);
        }

        //计算帖子分数
        if (entityType == ENTITY_TYPE_POST) {
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }


        return CommunityUtil.getJSONString(0, null, map);
    }

}
