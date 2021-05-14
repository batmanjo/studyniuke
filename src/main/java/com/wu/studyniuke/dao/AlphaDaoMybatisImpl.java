package com.wu.studyniuke.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @author me
 * @create 2021-05-11-19:40
 */
@Repository()
@Primary
public class AlphaDaoMybatisImpl implements AlphaDao{
    @Override
    public String select() {
        return "hello,Mybatis";
    }
}
