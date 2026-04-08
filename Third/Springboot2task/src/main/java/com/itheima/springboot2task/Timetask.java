package com.itheima.springboot2task;

import java.util.Timer;
import java.util.TimerTask;

public abstract class Timetask extends TimerTask {

//java当中的任务调度  每隔一秒钟时间执行run方法内容
    public static void main(String[] args) {
        Timer task = new Timer();
        Timetask timetask = new Timetask() {
            @Override
            public void run() {
                System.out.println("hello world");
            }
        };
        task.schedule(timetask, 0, 1000);
    }

}