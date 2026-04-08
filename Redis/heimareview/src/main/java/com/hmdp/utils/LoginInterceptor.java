package com.hmdp.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        判断是否需要拦截（ThreadLocal中是否有用户）
        if(UserHolder.getUser() == null){
//          如果没有用户的话就拦截，设置状态码
            response.setStatus(401);
//            拦截
            return false;
        }
//        由用户的话就放行
        return true;
    }



}
