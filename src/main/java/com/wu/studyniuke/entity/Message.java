package com.wu.studyniuke.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author me
 * @create 2021-05-30-16:16
 */
@Data
@ToString
public class Message {
    private int id;
    private int fromId;
    private int toId;
    private String conversationId;
    private String content;
    private int status;
    private Date createTime;
}
