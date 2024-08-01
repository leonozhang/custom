package com.leon.spring.component;

import com.leon.spring.annotation.Autowired;
import com.leon.spring.annotation.Component;
import com.leon.spring.annotation.Scope;
import com.leon.spring.processor.InitializingBean;

// 通过注解标识，将monsterService添加至spring容器中
@Component(value = "monsterService")
@Scope(value = "prototype")
public class MonsterService implements InitializingBean {

    @Autowired
    private MonsterDao monsterDao;

    public void m1(){
        monsterDao.sayHello();
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("MonsterService 初始化方法被调用...");
    }
}
