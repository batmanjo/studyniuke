package com.wu.studyniuke.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author me
 * @create 2021-05-12-22:28
 */
@Data
public class User {
    private int id;
    private String username;
    private String password;
    private String salt;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;



}
