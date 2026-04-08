package com.hmdp;

import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

@SpringBootTest
class HmDianPingApplicationTests {
    @Autowired
    private UserMapper userMapper;
    @Test
    void testSelect(){
        User user1 = userMapper.selectById(1);
        System.out.println(user1);
        
        User user2 = new User();
        user2.setPhone("13800000000");
        user2.setPassword("123456");
        user2.setNickName("测试用户");
        user2.setIcon("https://img.hmdp.com/1645200000000.png");
        userMapper.insert(user2);
        System.out.println(user2);
    }
    
    @Test
    void testDeleteBatchIds() {
        // 批量删除表中id200到500的数据
        List<Integer> ids = IntStream.rangeClosed(200, 500)
                .boxed()
                .collect(Collectors.toList());
        userMapper.deleteBatchIds(ids);
        System.out.println("删除成功，共删除 " + ids.size() + " 条记录");
    }


}