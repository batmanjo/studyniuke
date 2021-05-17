package com.wu.studyniuke;

import com.wu.studyniuke.util.MailClientUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * @author me
 * @create 2021-05-17-20:00
 */
@SpringBootTest
@ContextConfiguration(classes = StudyniukeApplication.class)
public class MailTest {

    @Autowired
    private MailClientUtil mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testSendEmail(){
        mailClient.sendMail("1205271990@qq.com","TEST","welcome");
    }

    @Test
    public void testSendHTMLEmail(){
        Context context = new Context();
        context.setVariable("username","老王");
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);
        mailClient.sendMail("1205271990@qq.com","HTML",content);
    }
}
