package com.itheima;

//import com.alicp.jetcache.anno.config.EnableCreateCacheAnnotation;
//import com.alicp.jetcache.anno.config.EnableMethodCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
//@EnableCreateCacheAnnotation
@EnableCaching
public class Springboot2ThirdApplication {
    public static void main(String[] args) {
        SpringApplication.run(Springboot2ThirdApplication.class, args);
    }
}
