package com.wu.studyniuke.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.wu.studyniuke.entity.DiscussPost;
import com.wu.studyniuke.entity.Event;
import com.wu.studyniuke.entity.Message;
import com.wu.studyniuke.service.DiscussPostService;
import com.wu.studyniuke.service.ElasticSearchService;
import com.wu.studyniuke.service.MessageService;
import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.CommunityUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.util.StringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

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

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${aliyun.bucket.share.name}")
    private String bucketShareName;

    @Value("${aliyun.bucket.share.url}")
    private String bucketShareUrl;

    @Value("${aliyun.endpoint.url}")
    private String endpoint;

    @Value("${aliyun.key.access}")
    private String accessKey;

    @Value("${aliyun.key.secret}")
    private String secretKey;

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

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
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

    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShare(ConsumerRecord record) {

        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空!");
            return;
        }

        Event event = JSON.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            LOGGER.error("消息格式错误！");
            return;
        }
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");
        String cmd =
                wkImageCommand
                        + " --quality 60 "
                        + htmlUrl
                        + " "
                        + wkImageStorage
                        + "/"
                        + fileName
                        + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            LOGGER.info("生成长图成功: " + cmd);
        } catch (IOException e) {
            LOGGER.error("生成长图失败: " + e.getMessage());
        }

        //启用定时器
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 1000);
        task.setFuture(future);
    }

    class UploadTask implements Runnable {

        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;
        // 启动任务的返回值
        private Future future;
        // 开始时间
        private long startTime;
        // 上传次数
        private int uploadTimes;

        public void setFuture(Future future) {
            this.future = future;
        }

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            // 生成失败
            if (System.currentTimeMillis() - startTime > 300000) {
                LOGGER.error("执行时间过长，终止任务: " + fileName);
                future.cancel(true);
                return;
            }
            // 上传失败
            if (uploadTimes >= 5) {
                LOGGER.error("上传次数过多，终止任务: " + fileName);
                future.cancel(true);
                return;
            }
            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()) {
                LOGGER.info(String.format("开始第%d次上传[%s].", ++uploadTimes, fileName));

                OSS ossClient = new OSSClientBuilder().build(endpoint, accessKey, secretKey);
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketShareName, fileName + suffix, file);
                ossClient.putObject(putObjectRequest);
                ossClient.shutdown();

                future.cancel(true);
            } else {
                LOGGER.info("等待图片生成[" + fileName + "].");
            }
        }
    }


}
