package com.wu.studyniuke.util;

import com.wu.studyniuke.entity.User;
import org.springframework.stereotype.Component;

/**
 * @author me
 * @create 2021-05-21-15:49
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    public User getUser(){
        return users.get();
    }

    public void setUser(User user){
        users.set(user);
    }

    public void clear(){
        users.remove();
    }
}
