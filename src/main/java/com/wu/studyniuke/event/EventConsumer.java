package com.wu.studyniuke.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.entity.Event;
import com.wu.studyniuke.entity.Message;
import com.wu.studyniuke.service.DiscussPostService;
import com.wu.studyniuke.service.ElasticSearchService;
import com.wu.studyniuke.service.MessageService;
import com.wu.studyniuke.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author me
 * @create 2021-07-19-19:51
 */
@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleSystemNotice(ConsumerRecord consumerRecord) {
        if (consumerRecord == null || consumerRecord.value() == null) {
            LOGGER.error("消息内容为空");
            return;
        }


        System.out.println("beforeParseEvent");
        Event event = JSON.parseObject(consumerRecord.value().toString(), Event.class);
        System.out.println(event.getData().entrySet().toString());


        if (event == null) {
            LOGGER.error("消息格式错误");
            return;
        }

        Message message = new Message();
        message.setFromId(SYSTEM_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if(!event.getData().isEmpty()){
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }

    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空!");
            return;
        }


        Event event = JSON.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            LOGGER.error("消息格式错误！");
            return;
        }
        // 搜索引擎保存帖子信息
        DiscussPost post = discussPostService.queryDiscussPost(event.getEntityId());
        elasticSearchService.saveDiscussPost(post);
    }


    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空!");
            return;
        }

        Event event = JSON.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            LOGGER.error("消息格式错误！");
            return;
        }

        elasticSearchService.deleteDiscussPost(event.getEntityId());
    }

}
