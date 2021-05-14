package com.wu.studyniuke.service;

import com.wu.studyniuke.dao.DiscussPostMapper;
import com.wu.studyniuke.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author me
 * @create 2021-05-13-11:18
 */
@Service
public class DiscussPortService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int selectDiscussPostRows(@Param("userId") int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }
}
