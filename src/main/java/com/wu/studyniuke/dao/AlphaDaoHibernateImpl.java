package com.wu.studyniuke.dao;

import org.springframework.stereotype.Repository;

/**
 * @author me
 * @create 2021-05-11-19:35
 */
@Repository("alphaDaoHibernate")
public class AlphaDaoHibernateImpl implements AlphaDao {
    @Override
    public String select() {
        return "hello,Hibernate";
    }
}
