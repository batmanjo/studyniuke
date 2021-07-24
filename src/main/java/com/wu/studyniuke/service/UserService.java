package com.wu.studyniuke.service;

import com.wu.studyniuke.dao.LoginTicketMapper;
import com.wu.studyniuke.dao.UserMapper;
import com.wu.studyniuke.entity.LoginTicket;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.CommunityUtil;
import com.wu.studyniuke.util.MailClientUtil;
import com.wu.studyniuke.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
//        return userMapper.selectUserById(id);
    }

    public User findUserByName(String name) {
        return userMapper.selectUserByName(name);
    }

    public Map<String, Object> register(User user) {
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

        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
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

    public int activation(int userId, String code) {
        User user = userMapper.selectUserById(userId);

        if (user.getStatus() == 1) {
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_REPEAT;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectUserByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证，即cookie
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        //ms为单位，需要乘以1000L。需要转换为long型，否则会发生数据丢失
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
//        loginTicketMapper.insertLoginTicket(loginTicket);


        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;

    }

    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket,1);

        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);

        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);

    }

    public LoginTicket findLoginTicket(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);

//        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeadUrl(int userId, String headUrl) {
/*        clearCache(userId);
        return userMapper.updateHeader(userId, headUrl);*/

        final int rows = userMapper.updateHeader(userId, headUrl);
        clearCache(userId);
        return rows;
    }

    //1、优先从缓存取值
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        User user = (User) redisTemplate.opsForValue().get(userKey);
        return user;
    }

    //2、取不到初始化
    private User initCache(int userId) {
        final User user = userMapper.selectUserById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user,3600, TimeUnit.SECONDS);

        return user;
    }

    //3、变更时清除缓存
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add((GrantedAuthority) () -> {
             switch (user.getType()){
                 case 2:
                     return AUTHORITY_MODERATOR;
                 case 1:
                     return AUTHORITY_ADMIN;
                 default:
                     return AUTHORITY_USER;
             }
        });
        return list;
    }
}
