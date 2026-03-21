package com.sabarno.chatomania.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisCacheConfig {

        @Bean
        RedisCacheConfiguration redisCacheConfiguration() {

                return RedisCacheConfiguration.defaultCacheConfig()
                                .serializeKeysWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                                .disableCachingNullValues()
                                .entryTtl(Duration.ofMinutes(5));
        }

        @Bean
        RedisCacheManager cacheManager(
                        RedisConnectionFactory connectionFactory,
                        RedisCacheConfiguration defaultConfig) {
                Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

                cacheConfigs.put("users",
                                defaultConfig.entryTtl(Duration.ofMinutes(10)));

                cacheConfigs.put("chatList",
                                defaultConfig.entryTtl(Duration.ofMinutes(5)));

                cacheConfigs.put("chatParticipants",
                                defaultConfig.entryTtl(Duration.ofMinutes(20)));

                cacheConfigs.put("presence",
                                defaultConfig.entryTtl(Duration.ofSeconds(30)));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultConfig)
                                .withInitialCacheConfigurations(cacheConfigs)
                                .build();
        }

        @Bean
        RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                template.setKeySerializer(new StringRedisSerializer());
                template.setHashKeySerializer(new StringRedisSerializer());

                template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
                template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

                template.afterPropertiesSet();
                return template;
        }
}
