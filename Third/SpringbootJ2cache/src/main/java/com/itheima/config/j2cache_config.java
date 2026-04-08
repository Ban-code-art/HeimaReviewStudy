package com.itheima.config;

import net.oschina.j2cache.CacheChannel;
import net.oschina.j2cache.J2Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class j2cache_config {
    @Bean
    public CacheChannel cacheChannel() {
        return J2Cache.getChannel();
    }
}
