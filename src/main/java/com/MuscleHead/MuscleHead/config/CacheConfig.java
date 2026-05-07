package com.MuscleHead.MuscleHead.config;

import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class CacheConfig {

    public static final String RECOMMENDED_USERS_CACHE = "recommendedUsers";

    /**
     * Redis connection for Spring Cache ({@code @Cacheable}). Uses the same Upstash URI as {@link RedisConfig}.
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(@Value("${upstash.redis.uri}") String redisUriString) {
        URI uri = URI.create(redisUriString);
        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 6379;

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(host);
        redisConfig.setPort(port);

        String userInfo = uri.getUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            String password = userInfo.contains(":")
                    ? userInfo.substring(userInfo.indexOf(':') + 1)
                    : userInfo;
            redisConfig.setPassword(password);
        }

        boolean useSsl = "rediss".equalsIgnoreCase(uri.getScheme());
        LettuceClientConfiguration.LettuceClientConfigurationBuilder clientBuilder = LettuceClientConfiguration.builder();
        if (useSsl) {
            clientBuilder.useSsl();
        }
        LettuceClientConfiguration clientConfig = clientBuilder.build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    @Bean
    public CacheManager cacheManager(
            LettuceConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .withCacheConfiguration(RECOMMENDED_USERS_CACHE, cacheConfiguration)
                .build();
    }
}
