package com.fatsnake.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import lombok.extern.java.Log;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @Auther: fatsnake
 * @Description":
 * @Date:2020-02-03 16:17
 * Copyright (c) 2020, zaodao All Rights Reserved.
 */
@Log
@Configuration
@EnableCaching // 开启缓存支持
public class RedisConfig {

    @Autowired
    private RedisProperties redisProperties;

    @Bean(destroyMethod = "destroy")
    public LettuceConnectionFactory redisConnectionFactory() {
        // redis单节点
        if (null == redisProperties.getCluster() || null == redisProperties.getCluster().getNodes()) {
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisProperties.getHost(),
                redisProperties.getPort());
            configuration.setPassword(redisProperties.getPassword());
            return new LettuceConnectionFactory(configuration);
        }

        // redis集群
        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(redisProperties.getCluster().getNodes());
        redisClusterConfiguration.setPassword(redisProperties.getPassword());
        redisClusterConfiguration.setMaxRedirects(redisProperties.getCluster().getMaxRedirects());

        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxTotal(redisProperties.getLettuce().getPool().getMaxActive());
        genericObjectPoolConfig.setMaxIdle(redisProperties.getLettuce().getPool().getMaxIdle());
        genericObjectPoolConfig.setMinIdle(redisProperties.getLettuce().getPool().getMinIdle());
        genericObjectPoolConfig.setMaxWaitMillis(redisProperties.getLettuce().getPool().getMaxWait().getSeconds());

        // 支持自适应集群拓扑刷新和动态刷新源
        ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .enableAllAdaptiveRefreshTriggers()
            // 开启自适应刷新
            .enableAdaptiveRefreshTrigger()
            // 开启定时刷新
            .enablePeriodicRefresh(Duration.ofSeconds(5))
            .build();

        ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
            .topologyRefreshOptions(clusterTopologyRefreshOptions).build();

        LettuceClientConfiguration lettuceClientConfiguration = LettucePoolingClientConfiguration.builder()
            .poolConfig(genericObjectPoolConfig)
//            .readFrom(ReadFrom.SLAVE_PREFERRED)  //读写分离：主写从读模式配置
            .clientOptions(clusterClientOptions).build();

        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisClusterConfiguration, lettuceClientConfiguration);
        lettuceConnectionFactory.setShareNativeConnection(false);// 是否允许多个线程操作共用同一个缓存连接，默认 true，false 时每个操作都将开辟新的连接
        lettuceConnectionFactory.resetConnection();// 重置底层共享连接, 在接下来的访问时初始化
        return lettuceConnectionFactory;
    }

    /**
     * RedisTemplate配置
     */
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(LettuceConnectionFactory factory) {
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




    @Bean
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuffer sb = new StringBuffer();
                sb.append(target.getClass().getName());
                sb.append(method.getName());
                for (Object obj : params) {
                    sb.append(obj.toString());
                }
                return sb.toString();
            }
        };
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
     *
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
