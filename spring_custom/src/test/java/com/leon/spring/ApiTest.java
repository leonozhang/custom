package com.leon.spring;

import com.leon.spring.annotation.AfterReturning;
import com.leon.spring.component.MonsterService;
import com.leon.spring.component.SmartAnimal;
import com.leon.spring.component.SmartAnimalAspect;
import com.leon.spring.ioc.SpringApplicationContext;
import com.leon.spring.ioc.SpringConfig;
import com.leon.spring.annotation.Before;
import org.junit.Test;

import java.lang.reflect.Method;

public class ApiTest {

    @Test
    public void applicationContextTest() {
        SpringApplicationContext aoc = new SpringApplicationContext(SpringConfig.class);

        MonsterService monsterService = (MonsterService) aoc.getBean("monsterService");

        monsterService.m1();
    }

    @Test
    public void aopTest() {
        SpringApplicationContext aoc = new SpringApplicationContext(SpringConfig.class);
        SmartAnimal smartDog = (SmartAnimal) aoc.getBean("smartDog");   // $Proxy,用接口接收代理对象实例
        System.out.println("smartDog = " + smartDog.getClass());
        float sum = smartDog.getSum(10, 20);
        System.out.println(sum);

        float sub = smartDog.getSub(10, 2);
        System.out.println(sub);
    }

    @Test
    public void aspectTest() throws Exception {
        // 获取切面类的class对象
        Class<SmartAnimalAspect> smartAnimalAspectClass = SmartAnimalAspect.class;

        // 遍历切面类所有方法
        for (Method declaredMethod : smartAnimalAspectClass.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(Before.class)) {
                // 切面类方法
                System.out.println("method = " + declaredMethod.getName());
                // 要切入的方法信息
                Before annotation = declaredMethod.getAnnotation(Before.class);
                System.out.println("annotation = " + annotation.value());

                // 获取要切入执行的方法
                Method method = smartAnimalAspectClass.getDeclaredMethod(declaredMethod.getName());

                // 反射执行切入方法
                method.invoke(smartAnimalAspectClass.newInstance(), null);

            } else if (declaredMethod.isAnnotationPresent(AfterReturning.class)) {
                System.out.println("method = " + declaredMethod.getName());
                AfterReturning annotation = declaredMethod.getAnnotation(AfterReturning.class);
                System.out.println("annotation = " + annotation.value());

                // 获取要切入执行的方法
                Method method = smartAnimalAspectClass.getDeclaredMethod(declaredMethod.getName());

                // 反射执行切入方法
                method.invoke(smartAnimalAspectClass.newInstance(), null);
            }
        }
    }
}
