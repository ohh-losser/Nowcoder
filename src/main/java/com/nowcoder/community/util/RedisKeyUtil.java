package com.nowcoder.community.util;

public class RedisKeyUtil {

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";

    // 某个实体的赞 key
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞 key
    // like:user:userId -> int
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体
    // followee:userId:entityType -> zset(entityId,now)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // follower:entityId:entityType -> zset(userId,now)
    public static String getFollowerKey(int entityId, int entityType){
        return PREFIX_FOLLOWER + SPLIT + entityId + SPLIT + entityType;
    }


    // 验证码 key
    // kaptcha:userId -> string
    public static String getKaptchaKey(String userId){
        return PREFIX_KAPTCHA + SPLIT + userId;
    }

    // 登录的凭证
    // ticket:userId -> string
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户 key
    // user:userId -> string
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }


}

