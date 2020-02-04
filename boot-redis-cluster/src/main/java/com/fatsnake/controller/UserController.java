package com.fatsnake.controller;

import com.fatsnake.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @Auther: fatsnake
 * @Description":
 * @Date:2020-02-04 08:12
 * Copyright (c) 2020, zaodao All Rights Reserved.
 */
@RestController
@RequestMapping("user")
public class UserController {


    @Autowired
    RedisTemplate redisTemplate;

    public static String keyPrefix = "user:";


    @GetMapping("getUser")
    public Map<String, Object> getUser(@RequestParam(defaultValue = "1") String id) {
        String key = keyPrefix + id;
        User user = null;
        if (redisTemplate.hasKey(key)) {
            user = (User) redisTemplate.opsForValue().get(key);
        } else { // 假装从数据库获取
            user = new User();
            user.setAge(31);
            user.setReadNum(111);
            user.setName("fatsnake");
            user.setId(id);
            redisTemplate.opsForValue().set(key, user);
        }

        Properties info = redisTemplate.getRequiredConnectionFactory().getConnection().info("Replication");


        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("info", info);

        return result;
    }

}
