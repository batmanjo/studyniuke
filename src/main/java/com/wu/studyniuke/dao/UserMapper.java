package com.wu.studyniuke.dao;

import com.wu.studyniuke.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author me
 * @create 2021-05-12-22:48
 */
@Mapper
public interface UserMapper {
    User selectUserById(int id);

    User selectUserByName(String name);

    User selectUserByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);

}
