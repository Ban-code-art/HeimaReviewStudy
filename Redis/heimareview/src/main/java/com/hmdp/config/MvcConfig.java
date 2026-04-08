package com.hmdp.config;

import com.hmdp.utils.RefreshTokenInterceptor;
import com.hmdp.utils.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class MvcConfig implements WebMvcConfigurer {
@Autowired
private RefreshTokenInterceptor refreshTokenInterceptor;
@Autowired
private LoginInterceptor loginInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(loginInterceptor)
               .excludePathPatterns(
                       "/shop/**",
                       "/voucher/**",
                       "/user/login",
                       "/shop-type/**",
                       "/upload/**",
                       "/user/code",
                       "/blog/hot"

               ).order(1);
       registry.addInterceptor(refreshTokenInterceptor).addPathPatterns("/**").order(0);
    }
}
