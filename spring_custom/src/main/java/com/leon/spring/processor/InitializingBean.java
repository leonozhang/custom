package com.leon.spring.processor;

// 当一个Bean实现此接口后，就实现afterPropertiesSet()方法，完成初始化
public interface InitializingBean {
    void afterPropertiesSet();
}
