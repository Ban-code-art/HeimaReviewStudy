package com.itheima;

import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCreateCacheAnnotation//jetcache启动缓存的主开关
@EnableMethodCache(basePackages = "com.itheima")//开启JetCache方法缓存 这个注解要和上面的那个注解配置在一起使用
public class Springboot2ThirdApplication {
    public static void main(String[] args) {
        SpringApplication.run(Springboot2ThirdApplication.class, args);
    }
}
