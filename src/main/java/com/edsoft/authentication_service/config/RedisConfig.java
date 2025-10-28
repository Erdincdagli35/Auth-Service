package com.edsoft.authentication_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            @Value("${spring.redis.host:localhost}") String host,
            @Value("${spring.redis.port:6379}") int port,
            @Value("${spring.redis.password:}") String password) {

        RedisStandaloneConfiguration conf = new RedisStandaloneConfiguration(host, port);
        if (password != null && !password.isBlank()) conf.setPassword(RedisPassword.of(password));
        return new LettuceConnectionFactory(conf);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory cf) {
        RedisTemplate<String, Object> rt = new RedisTemplate<>();
        rt.setConnectionFactory(cf);
        rt.setKeySerializer(new StringRedisSerializer());
        rt.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return rt;
    }
}