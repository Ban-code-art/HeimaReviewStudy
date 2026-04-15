package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock {
    private String name;
    private StringRedisTemplate stringRedisTemplate;
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    private static final String KEY_PREFIX = "lock:";
    /*
    * long指的是返回值的类型，defaultredisscript是redis脚本的实现类
    * */
    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();//初始化
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));//classpathresource是resource的实现类，用于加载classpath下的资源文件
        UNLOCK_SCRIPT.setResultType(Long.class);//设置返回值的类型
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        //获取线程标识 和uuid结合起来
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        //获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId + "", timeoutSec, TimeUnit.SECONDS);
        /*这里的success是Boolean类型的，如果直接返回会自动拆箱可能会造成安全问题*/
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
//   使用lua脚本的方式释放锁
        stringRedisTemplate.execute(UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX + name),
                ID_PREFIX + Thread.currentThread().getId());
    }
    /*
    @Override
    public void unlock() {
        //获取线程标识 和uuid结合起来
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        //获取锁中的标识
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
        //判断标识是否一致
        if (threadId.equals(id)) {
            //释放锁
            stringRedisTemplate.delete(KEY_PREFIX + name);
        }

    }*/
}
