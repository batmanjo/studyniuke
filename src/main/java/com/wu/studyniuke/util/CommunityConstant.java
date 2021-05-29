package com.wu.studyniuke.util;

/**
 * @author me
 * @create 2021-05-17-21:39
 */
public interface CommunityConstant {

    int ACTIVATION_SUCCESS = 0;
    int ACTIVATION_REPEAT = 1;
    int ACTIVATION_FAILURE = 2;

    int ENTITY_TYPE_POST=1;
    int ENTITY_TYPE_COMMENT=2;

    //默认存储时间
    int DEFAULT_EXPIRED_SECONDS=3600*12;
    //勾选后的登录凭证超时时间
    int REMEMBER_EXPIRED_SECONDS=3600*24*100;
}
