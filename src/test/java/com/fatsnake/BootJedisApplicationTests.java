package com.fatsnake;

import com.fatsnake.Service.IUserService;
import com.fatsnake.config.JedisConfig;
import com.fatsnake.po.User;
import com.oracle.tools.packager.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BootJedisApplicationTests {


    @Autowired
    private JedisConfig jedisConfig;

    @Autowired
    private IUserService userService;


    @Test
    void contextLoads() {
        System.out.println(jedisConfig);
    }


    @Test
    void t1() {
        String result = userService.getString("name");
        System.out.println(result);
    }

    @Test
    void t2() {
        String key = "testKey";
        String value = "测试数据";
        userService.expireStr(key, value);
        Log.info("设置有效期的值成功");
    }


    @Test
    void t3() {
        User user = userService.selectById("1001");
        System.out.println(user);
    }
}
