package com.leon.spring.ioc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// 封装Bean信息[scope、Bean对应的额Class对象]
@Getter
@Setter
@ToString
public class BeanDefinition {
    private String scope;
    private Class<?> clazz;
}
