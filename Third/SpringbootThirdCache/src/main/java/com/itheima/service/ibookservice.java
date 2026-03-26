package com.itheima.service;

import com.itheima.pro.book;

import java.util.List;

public interface ibookservice {
    List<book> getbook();
    book getById(Integer id);
    boolean save(book book);
    boolean update(book book);
    boolean delete(Integer id);
    List<book> getAll();
}
