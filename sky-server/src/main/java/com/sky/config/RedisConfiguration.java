package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 34255
 * Date: 2025-05-14
 * Time: 11:37
 */
@Slf4j
@Configuration
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){

        log.info("开始创建RedisTemplate模板对象");

        //创建RedisTemplata对象
        RedisTemplate redisTemplate = new RedisTemplate();

        //设置Redis的连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        //设置Redis key序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        //设置Redis value序列化器
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
