package com.leon.spring.processor;

// bean后置处理器，对所有spring容器中的Bean生效 ==> 为生成的bean赋值或添加方法等
public interface BeanPostProcessor {
    // 在bean的初始化方法前调用
    // 接收bean对象，对bean对象处理后，返回处理后的bean
    default Object postProcessBeforeInitialization(Object bean, String beanName){
        return bean;
    }

    // 在bean的初始化方法后调用
    default Object postProcessAfterInitialization(Object bean, String beanName){
        return bean;
    }
}
