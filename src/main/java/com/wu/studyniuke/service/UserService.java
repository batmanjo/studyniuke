package com.wu.studyniuke.service;

import com.wu.studyniuke.dao.UserMapper;
import com.wu.studyniuke.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author me
 * @create 2021-05-13-11:23
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id){
        return userMapper.selectUserById(id);
    }

    public User findUserByName(String name){
        return userMapper.selectUserByName(name);
    }
}
