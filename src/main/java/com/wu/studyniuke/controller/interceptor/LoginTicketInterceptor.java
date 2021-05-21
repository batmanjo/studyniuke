package com.wu.studyniuke.controller.interceptor;

import com.wu.studyniuke.entity.LoginTicket;
import com.wu.studyniuke.entity.User;
import com.wu.studyniuke.service.UserService;
import com.wu.studyniuke.util.CookieUtil;
import com.wu.studyniuke.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;


/**
 * @author me
 * @create 2021-05-20-23:11
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request, "ticket");

        if (ticket != null) {
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            if (loginTicket != null) {
                if (loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                    User user = userService.findUserById(loginTicket.getUserId());
                    //存User
                    hostHolder.setUser(user);
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser",user);
        }
    }
}
