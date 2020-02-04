package com.fatsnake;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class BootRedisClusterApplicationTests {


    @Autowired
    RedisTemplate redisTemplate;


    @Test
    void contextLoads() {
        System.out.println(redisTemplate);
    }


    @Test
    void test1() {
        redisTemplate.opsForValue().set("name", "zhangsan");
    }


    @Test
    void test2() {
        String name = redisTemplate.opsForValue().get("name").toString();
        System.out.println(name);
    }


    @Test
    void test3() {

        System.out.println(redisTemplate.getRequiredConnectionFactory().getConnection().info("clients"));
    }


}
