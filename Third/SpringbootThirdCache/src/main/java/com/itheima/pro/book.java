package com.itheima.pro;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
//@TableName("table_book")
public class book implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String type;
    private String description;
}
