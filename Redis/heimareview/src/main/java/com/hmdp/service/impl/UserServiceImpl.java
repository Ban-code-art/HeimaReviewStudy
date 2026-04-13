package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

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
    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
//        4.保存验证码到Redis
//        session.setAttribute("code",code);  这边是引入了工具类中的常量
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code,LOGIN_CODE_TTL, TimeUnit.MINUTES);
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
//        3.校验验证码  从Redis中获取验证码和前端页面当中的表单验证码进行比对
//        Object cacheCode = session.getAttribute("code");
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
//        4.如果验证码不正确，返回错误信息
            return Result.fail("验证码错误");
        }
//        5.如果验证码正确,根据手机号在数据库里查询用户 select * from user where phone = ? limit 1
        User user = query().eq("phone", phone).one();
//        6.判断用户是否存在
//        6.1.如果不存在，那么就创建用户并保存在数据库
        if (user == null) {
            // 这里使用一个方法创建用户并保存在数据库
            user = createUserWithPhone(phone);
        }
//        7.将用户信息保存在Redis中

//        7.1 随机生成token，作为登录令牌（使用uuid作为token）-> 这里使用hutool的uuid工具类
        String token = UUID.randomUUID().toString(true);//不包含横线的uuid

//        7.2将User对象转为Hash存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);//将User对象转为UserDTO对象

        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString())); // 忽略空值，将所有字段转换为字符串

        String tokenKey = LOGIN_USER_KEY + token;
//        7.3存储
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
//        7.4设置token的有效期  这里设置的token有效期是登陆之后的有效期，如果要刷新的话就要重新登陆，但是使用拦截器可以刷新token
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
//        返回token给前端页面
        return Result.ok(token);
        }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName("用户" + RandomUtil.randomString(10));
        save(user);
        return user;
    }

}
