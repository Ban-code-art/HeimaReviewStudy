package com.itheima.Redis.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
//实现序列化
public class student implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private Integer age;
}
