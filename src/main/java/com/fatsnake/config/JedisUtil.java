package com.fatsnake.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @Auther: fatsnake
 * @Description":
 * @Date:2020-01-25 10:41
 * Copyright (c) 2020, zaodao All Rights Reserved.
 */
@Component
public class JedisUtil {

    /**
     * 连接池
     */
    @Autowired
    private JedisPool jedisPool;


    /**
     * 获取连接
     *
     * @return Jedis
     */
    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    /**
     * 关闭连接
     *
     * @param jedis jedis
     */
    public void closeJedis(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    /**
     * 将小时转化成秒
     *
     * @param hours hours
     * @return long
     */
    public int calTimeHours(int hours) {
        return hours * 60 * 60;
    }
}
