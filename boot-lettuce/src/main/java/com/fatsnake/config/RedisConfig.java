package com.fatsnake.config;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * @Auther: fatsnake
 * @Description":
 * @Date:2020-01-27 09:23
 * Copyright (c) 2020, zaodao All Rights Reserved.
 */
@Log
@Configuration
@EnableCaching
public class RedisConfig {


    /**
     * 自定义缓存key生成策略 KeyGenerator
     * 默认的生成策略时看不懂的（乱码内容）通过Srping的依赖注入特性进行自定义的配置注入并且此类是配置类可以更多程度的自定义配置
     *
     * @return KeyGenerator
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return (o, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(o.getClass().getName()); // 类目
            sb.append(method.getName()); // 方法名
            for (Object param : params) {
                sb.append(param.toString()); // 参数名
            }
            return sb.toString();
        };
    }

    // 配置缓存管理器

    /**
     * h
     *
     * @param factory
     * @return
     */
    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory factory) {
        // 以锁写入的方式创建 RedisCacheWriter对象
        RedisCacheWriter wirter = RedisCacheWriter.lockingRedisCacheWriter(factory);
        // 创建默认缓存配置对象
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

        RedisCacheManager redisCacheManager = new RedisCacheManager(wirter, config);

        log.info("自定义RedisCacheManager加载完成");
        return redisCacheManager;
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        // 使用注解@Bean返回RedisTemplate的时候，同时配置hashkey和hashValue的序列虎方式
        // key采用String的序列化方式
        redisTemplate.setKeySerializer(keySerializer());
        // value使用jackson序列化方式
        redisTemplate.setValueSerializer(valueSerializer());

        // hash的key采用String的序列化方式
        redisTemplate.setHashKeySerializer(keySerializer());
        // hash的value使用jackson序列化方式
        redisTemplate.setHashValueSerializer(valueSerializer());

        /**必须执行这个函数,初始化RedisTemplate*/
        // 需要先调用afterPropertiesSet方法,此方法是应该是初始化参数和初始化工作。
        redisTemplate.afterPropertiesSet();
        log.info("序列化完成！");
        return redisTemplate;
    }


    /**
     * key键序列化方式
     *
     * @return RedisSerializer
     */
    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }


    /**
     * value值序列化方式
     * @return
     */
    private Jackson2JsonRedisSerializer valueSerializer() {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        return jackson2JsonRedisSerializer;
    }
}
