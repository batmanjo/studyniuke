package com.wu.studyniuke.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author me
 * @create 2021-05-13-10:10
 */
@Data
@ToString
public class DiscussPost {
    private int id;
    private int userId;
    private String title;
    private String content;
    private int type;
    private int status;
    private Date createTime;
    private int commentCount;
    private double score;



}
