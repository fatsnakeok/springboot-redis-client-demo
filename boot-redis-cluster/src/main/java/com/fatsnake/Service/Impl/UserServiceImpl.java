package com.fatsnake.Service.Impl;

import com.fatsnake.Service.IUserService;
import com.fatsnake.po.User;
import lombok.extern.java.Log;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.TimeUnit;


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
     */
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 简化写法
     */
    @Resource(name = "redisTemplate")
    ValueOperations<String, String> string;


//    @Resource
//    // If you add a @Bean of your own of any of the auto-configured types it will replace the default (except in
//    // the case of RedisTemplate the exclusion is based on the bean name ‘redisTemplate’ not its type).
////    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;


    @Override
    public String getString(String key) {
        String val = null;

        if (redisTemplate.hasKey(key)) {
            val = redisTemplate.opsForValue().get(key).toString();
            log.info("使用redisTemplate，查询的是redis中的数据");
        } else {
            val = "fatsnake";
            log.info("查询的是mysql中的数据");
            redisTemplate.opsForValue().set(key, val);
            log.info("使用redisTemplate，存入redis中");

        }
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
        // 简化写法
//        redisTemplate.opsForValue().set(key, value);
        string.set(key, value);
        redisTemplate.expire(key, 2, TimeUnit.HOURS); // 2天 有效期
        log.info("使用redisTemplate，存入redis中");
    }

    /**
     * hssh 测试
     *
     * @param id
     * @return User
     */
    @Override
    public User selectById(String id) {

        if (redisTemplate.opsForHash().hasKey("user", id)) {
            log.info("使用redisTemplate，查询的是redis中的数据");
            return (User) redisTemplate.opsForHash().get("user", id);
        } else {
            User user = new User();
            user.setId(id);
            user.setName("fatsnake");
            user.setAge(33);
            log.info("从Mysql中获取用户：" + user);
            redisTemplate.opsForHash().put("user", id, user);
            log.info("使用redisTemplate，以hash方式存入redis中");
            return user;
        }
    }

    /**
     * 更新用户阅读量
     *
     * @param id
     * @return int
     */
    @Override
    public Long incrementReadCount(String id) {
        String key = "user:" + id;
        if (redisTemplate.opsForHash().hasKey("user", id)) {
            log.info("使用redisTemplate，查询的是redis中的数据,增加阅读量");
            // 增加阅读量
            return redisTemplate.opsForHash().increment(key, "readNum", 1);
        } else {
            User user = new User();
            user.setId(id);
            user.setName("fatsnake");
            user.setAge(33);
            user.setReadNum(0);
            Map map = new BeanMap(user);
            redisTemplate.opsForHash().putAll(key, map);
            return redisTemplate.opsForHash().increment(key, "readNum", 1);
        }
    }

    @Override
    public User getHashUserById(String id) {
        String key = "user:" + id;
        User user = null;
        try {
            user = new User();
            BeanUtils.populate(user, redisTemplate.opsForHash().entries(key));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return user;
    }
}
