package com.leon.spring.component;

import com.leon.spring.annotation.AfterReturning;
import com.leon.spring.annotation.Aspect;
import com.leon.spring.annotation.Before;
import com.leon.spring.annotation.Component;

// SmartAnimal接口的切面类
@Aspect
@Component
public class SmartAnimalAspect {
    // 前置通知(切入表达式 全类名 方法)
    @Before(value = "execution com.leon.spring.component.SmartDog getSum")
    public static void showBeginLog() {
        System.out.println("前置通知...");
    }

    @AfterReturning(value = "execution com.leon.spring.component.SmartDog getSum")
    public static void showSuccessLog(){
        System.out.println("返回通知...");
    }
}
