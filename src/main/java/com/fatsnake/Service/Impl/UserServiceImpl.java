package com.fatsnake.Service.Impl;

import com.fatsnake.Service.IUserService;
import com.fatsnake.config.JedisUtil;
import com.fatsnake.po.User;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: fatsnake
 * @Description":
 * @Date:2020-01-24 19:15
 * Copyright (c) 2020, zaodao All Rights Reserved.
 */
@Log   //  private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
@Service
public class UserServiceImpl implements IUserService {

    /**
     * jedis
     * <p>
     * redis中有什么方法，jedis中就有什么方法
     */
    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private JedisUtil jedisUtil;

    @Override
    public String getString(String key) {
        String val = null;

        Jedis jedis = jedisPool.getResource();
        if (jedis.exists(key)) {
            val = jedis.get(key);
            log.info("查询的是redis中的数据");
        } else {
            val = "fatsnake";
            log.info("查询的是mysql中的数据");
            jedis.set(key, val);
        }

        // 关闭连接
        jedis.close();
        return val;
    }

    /**
     * 设置有效期的值
     *
     * @param key
     * @param value
     */
    @Override
    public void expireStr(String key, String value) {
        // 使用工具类获取连接，关闭连接
        Jedis jedis = jedisUtil.getJedis();
        jedis.set(key, value); // 永久有效
//        jedis.expire(key, jedisUtil.calTimeHours(28)); // 设置28小时有效
        jedis.expire(key, 20); // 设置20秒有效
        log.info(key + "\t设置值：" + value + "\t");
        jedisUtil.closeJedis(jedis);
    }

    @Override
    public User selectById(String id) {
        User user = new User();
        String key = "user:" + id;
        Jedis jedis = jedisUtil.getJedis();
        Map<String, String> map = null;
        if (jedis.exists(key)) {
            map = jedis.hgetAll(key);
            user.setId(map.get("id"));
            user.setName(map.get("name"));
            user.setAge(Integer.parseInt(map.get("age")));
            log.info("从redis中获取用户：" + user);
        } else {
            user.setId(id);
            user.setName("fatsnake");
            user.setAge(33);
            log.info("从Mysql中获取用户：" + user);
            map = new HashMap<>();
            map.put("id", user.getId());
            map.put("name", user.getName());
            map.put("age", user.getAge().toString());
            jedis.hset(key, map);
        }

        jedisUtil.closeJedis(jedis);
        return user;
    }
}
