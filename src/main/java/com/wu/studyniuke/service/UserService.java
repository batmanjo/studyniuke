package com.wu.studyniuke.service;

import com.wu.studyniuke.dao.UserMapper;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.CommunityUtil;
import com.wu.studyniuke.util.MailClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author me
 * @create 2021-05-13-11:23
 */
@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClientUtil mailClientUtil;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;
    public User findUserById(int id){
        return userMapper.selectUserById(id);
    }

    public User findUserByName(String name){
        return userMapper.selectUserByName(name);
    }

    public Map<String,Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不一致");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        User user1 = userMapper.selectUserByName(user.getUsername());
        if (user1 != null) {
            map.put("usernameMsg", "账号存在");
            return map;
        }

        user1 = userMapper.selectUserByEmail(user.getEmail());
        if (user1 != null) {
            map.put("emailMsg", "邮箱存在");
            return map;
        }

        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword())+user.getSalt());
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());

        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件,使用thymeleaf创建的对象携带变量
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:9999/wu/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        //使用模板引擎，利用thymeleaf，将context放到/mail/activation.html文件中，然后再利用mail包发送给邮箱
        String content = templateEngine.process("/mail/activation", context);
        mailClientUtil.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    public int activation(int userId,String code){
        User user = userMapper.selectUserById(userId);

        if(user.getStatus()==1){
            return ACTIVATION_SUCCESS;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            return ACTIVATION_REPEAT;
        }else{
            return ACTIVATION_FAILURE;
        }
    }
}
