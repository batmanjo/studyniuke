package com.wu.studyniuke;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.HashMap;
import java.util.Map;

/**
 * @author me
 * @create 2021-07-18-21:25
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = StudyniukeApplication.class)
public class KafkaTest {

    @Autowired
    private KafkaProducer kafkaProducer;


    @Test
    public void testKafka() {
        kafkaProducer.sendMessage("test", "你好");
        kafkaProducer.sendMessage("test", "在哪");
        kafkaProducer.sendMessage("test", "带饭");

        try {
            Thread.sleep(1000 * 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    @Test
    public void testSerializer() {

        Map<String,Object> map = new HashMap<>();
        map.put("userId",1);
        map.put("userName","zhang");

        Integer integer = new Integer(1234);

        System.out.println(JSONObject.toJSONString(map));
        System.out.println("是否显示消息");
        String s = JSONObject.toJSONString(map);
        Map<String,Object> map1 = JSON.parseObject(s,HashMap.class);

        System.out.println(map1.get("userId"));
        kafkaProducer.sendMessage("test", JSONObject.toJSONString(integer));
        kafkaProducer.sendMessage("test", JSONObject.toJSONString(map));

        try {
            Thread.sleep(1000 * 3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }




}

@Component
class KafkaProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic, content);
    }
}


@Component
class KafkaConsumer {
    @KafkaListener(topics = ("test"))
    public void handMessage(ConsumerRecord consumerRecord) {
        System.out.println("进入kafkaConsumer");
        System.out.println(consumerRecord.value());


    }
}


