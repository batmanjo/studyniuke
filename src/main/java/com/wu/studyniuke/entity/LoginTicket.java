package com.wu.studyniuke.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author me
 * @create 2021-05-18-14:05
 */
@Data
@ToString
public class LoginTicket {
    private int id;
    private int userId;
    private String ticket;
    private int status;
    private Date expired;

}
