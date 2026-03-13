package com.nowcoder.community.util;

public interface CommunityConstant {
    // 激活成功 常量大写命名
    int ACTIVATION_SUCCESS = 0;

    // 重复激活
    int ACTIVATION_REPEAT = 1;

    // 激活失败
    int ACTIVATION_FAILURE = 2;

    // 默认状态的登录凭证超时时间
    int DEFAULT_EXPIRED_SECONDS = 12 * 60 * 60;

    // 记住状态的登录凭证超时时间
    int REMEMBER_EXPIRED_SECONDS = 24 * 60 * 60 * 100;

    //帖子
    int ENTITY_TYPE_POST = 1;

    //评论
    int ENTITY_TYPE_COMMENT = 2;
}
