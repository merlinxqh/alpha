package com.leo.alpha.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leo on 2017/4/29.
 */
@Configuration
public class RedisConfig {

    @Autowired
    private RedisProperties redis;

    /**
     * Jedis 连接池配置
     * @return
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig(){
        JedisPoolConfig config=new JedisPoolConfig();
        config.setMinIdle(redis.getMinIdle());
        config.setMaxIdle(redis.getMaxIdle());
        config.setMaxWaitMillis(redis.getMaxWaitMillis());
        config.setTestOnBorrow(redis.isTestOnBorrow());
        config.setTestOnReturn(redis.isTestOnReturn());
        config.setTestWhileIdle(redis.isTestWhileIdle());
        return config;
    }

    /**
     * jedis连接工厂配置
     * @return
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory(){
        JedisConnectionFactory factory=new JedisConnectionFactory(jedisPoolConfig());
        factory.setTimeout(redis.getTimeout());
        factory.setPassword("hiveview");
        factory.setHostName("localhost");
        factory.setPort(6379);
        return factory;
    }

    /**
     * redis监听哨兵
     * @return
     */
//    @Bean
//    public RedisSentinelConfiguration redisSentinelConfiguration(){
//        RedisSentinelConfiguration sentinel=new RedisSentinelConfiguration();
//        //redis主库节点
//        RedisNode masterNode=new RedisNode(redis.getMasterHost(), redis.getMasterPort());
//        masterNode.setName(redis.getMasterName());
//        sentinel.setMaster(masterNode);
//
//        //sentinel监听节点
//        List<RedisNode> sentinels=new ArrayList<>();
//        for(String host:redis.getSentinelHost().split(",")){
//        	RedisNode sentinelNode=new RedisNode(host.split(":")[0], Integer.valueOf(host.split(":")[1]));
//            sentinels.add(sentinelNode);
//        }
//        sentinel.setSentinels(sentinels);
//        return sentinel;
//    }

    @Bean
    public StringRedisTemplate redisTemplate(){
       StringRedisTemplate redisTemplate=new StringRedisTemplate();
       redisTemplate.setConnectionFactory(jedisConnectionFactory());
       return redisTemplate;
    }

}
