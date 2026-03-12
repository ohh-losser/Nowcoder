package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

public class CommunityUtil {

    //生成随机字符串

    public static String generateUUID() {
        return java.util.UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密

    public static String md5(String key) {
        if(StringUtils.isBlank(key)) {
            return null;
        }

        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if(map != null) {
            for(String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    //重载
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    //重载 只有code参数
    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "张三");
        map.put("age", 18);
        System.out.println(getJSONString(0, "成功", map));
    }

}
