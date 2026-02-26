package com.MuscleHead.MuscleHead.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {

    @Value("${upstash.redis.uri}")
    private String redisUri;

    @Bean
    public JedisPool jedisPool() {
        URI uri = URI.create(redisUri);
        return new JedisPool(new JedisPoolConfig(), uri);
    }
}
