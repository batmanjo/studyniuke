package com.wu.studyniuke.util;

/**
 * @author me
 * @create 2021-07-03-20:30
 */

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_CODE = "code";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    public static final String PREFIX_UV = "uv" ;
    public static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";
    private static final String PREFIX_RATE_LIMIT_PUBLISH = "rate:limit:publish";

    //某个实体的赞
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //某个用户的赞
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体
    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityType:entityId -> zset(userId,now)
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    public static String getCodeKey(String owner) {
        return PREFIX_CODE + SPLIT + owner;
    }

    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }

    public static String getUVKey(String start,String end){
        return PREFIX_UV + SPLIT +start + SPLIT +end;
    }

    public static String getDAUKey(String date){
        return PREFIX_DAU +SPLIT +date;
    }

    public static String getDAUKey(String start,String end){
        return PREFIX_DAU + SPLIT +start + SPLIT +end;
    }

    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

    public static String getRateLimitKey(int userId) {
        return PREFIX_RATE_LIMIT_PUBLISH + SPLIT + userId;
    }
}
