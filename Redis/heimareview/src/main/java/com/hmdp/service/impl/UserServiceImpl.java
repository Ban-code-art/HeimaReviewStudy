package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
//    校验手机号码
    @Override
    public Result sendCode(String phone, HttpSession session) {
//        1.校验手机号格式
        if (RegexUtils.isPhoneInvalid(phone)) {
//            2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
//        3.格式符合的话生成验证码（使用hutool生成6位随机数字）
        String code = RandomUtil.randomNumbers(6);
//        4.保存验证码到session
        session.setAttribute("code",code);
//        5.发送验证码 暂时不做
        log.debug("发送短信验证码成功，验证码为{}",code);
//        6.返回结果

        return Result.ok();
    }
//    登录
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
//        1.校验手机号格式
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
//            2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
//        3.校验验证码
        Object cacheCode = session.getAttribute("code");
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.toString().equals(code)) {
//        4.如果验证码不正确，返回错误信息
            return Result.fail("验证码错误");
        }
//        5.如果验证码正确,根据手机号查询用户 select * from user where phone = ? limit 1
        User user = query().eq("phone", phone).one();
//        6.判断用户是否存在
//        6.1.如果不存在，那么就创建用户并保存在数据库
        if (user == null) {
            // 这里使用一个方法创建用户并保存在数据库
            user = createUserWithPhone(phone);
        }
//        将用户信息保存在session中
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok(user);
        }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("用户" + RandomUtil.randomString(10));
        save(user);
        return user;
    }

}
