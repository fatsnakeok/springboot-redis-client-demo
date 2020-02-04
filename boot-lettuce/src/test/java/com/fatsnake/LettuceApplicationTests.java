package com.fatsnake;

import com.fatsnake.Service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class LettuceApplicationTests {

    @Autowired
    private IUserService userService;

    @Autowired
    RedisTemplate redisTemplate;


    @Test
    void contextLoads() {
    }


    @Test
    void test1() {
        userService.getString("lettuceTest");
    }

    @Test
    void test2() {
        userService.expireStr("lettuceTest", "lettuceTest的有效期测试数据");
    }

    @Test
    void test3() {
        System.out.println(userService.selectById("1234554321"));
    }

    @Test
    void test4() {
        System.out.println(userService.incrementReadCount("24008"));
    }

    @Test
    void test5() {
        System.out.println(userService.getHashUserById("24008"));
    }

    @Test
    void test6() {
        System.out.println(redisTemplate.opsForValue().get("name"));
    }
}
