package com.hmdp.utils;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        获取session
        HttpSession session = request.getSession();
//        获取session中的用户
        Object user = session.getAttribute("user");
//        判断用户是否存在
        if(user == null){
//            不存在，拦截，返回401状态码
            response.setStatus(401);
            return false;
        }
//        存在，保存用户信息到ThreadLocal  使用外部类
        UserHolder.saveUser((UserDTO) user);
//        放行
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

//        移除用户，防止内存泄漏
        UserHolder.removeUser();

    }
}
