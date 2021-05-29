package com.wu.studyniuke.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author me
 * @create 2021-05-29-20:54
 */
@Data
@ToString
public class Comment {
    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private int targetId;
    private String content;
    private int status;
    private Date createTime;

}
