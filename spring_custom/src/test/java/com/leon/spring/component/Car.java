package com.leon.spring.component;

import com.leon.spring.annotation.Component;
import com.leon.spring.processor.InitializingBean;

@Component
public class Car implements InitializingBean {
    @Override
    public void afterPropertiesSet() {
        System.out.println("Car 初始化方法被调用...");
    }
}
