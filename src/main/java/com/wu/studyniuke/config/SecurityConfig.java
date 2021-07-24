package com.wu.studyniuke.config;

import com.wu.studyniuke.util.CommunityConstant;
import com.wu.studyniuke.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * @author me
 * @create 2021-07-24-15:16
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//
//    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR,
                        AUTHORITY_USER
                ).anyRequest().permitAll()
                .and().csrf().disable();


        //权限不足时处理
        http.exceptionHandling()
                .accessDeniedHandler(new AccessDeniedHandler() {
                    //没有权限
                    @Override
                    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                        String header = httpServletRequest.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(header)) {
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "没有权限"));
                        } else {
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/denied");
                        }
                    }
                })
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    //没有登录
                    @Override
                    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                        String header = httpServletRequest.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(header)) {
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "没有登录"));
                        } else {
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
                        }
                    }
                });

        //security默认拦截logout,但我要用自己的
        //骗你去拦截404,哈哈
        http.logout().logoutUrl("/security/logout");
    }


}
