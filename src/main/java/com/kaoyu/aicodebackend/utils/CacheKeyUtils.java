package com.kaoyu.aicodebackend.utils;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存键 key 生成工具类
 *
 */
public class CacheKeyUtils {

    /**
     * 生成缓存键 key
     *
     * @param obj 生成缓存key的对象
     * @return MD5哈希后的缓存key
     */
    public static String generateCacheKey(Object obj) {
        if (obj == null) {
            //空值也要生成缓存key，防止多次查询
            return DigestUtil.md5Hex("null");
        }
        //先转为json字符串
        String jsonStr = JSONUtil.toJsonStr(obj);
        //生成md5哈希值
        return DigestUtil.md5Hex(jsonStr);
    }


}
