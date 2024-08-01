package com.leon.spring.component;

import com.leon.spring.annotation.Component;
import com.leon.spring.processor.BeanPostProcessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // System.out.println("后置处理器Before方法被调用, Bean类型 = "
        //         + bean.getClass() + " beanName = " + beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // System.out.println("后置处理器After方法被调用, Bean类型 = "
        //         + bean.getClass() + " beanName = " + beanName);

        // 实现aop，返回代理对象(对bean的方法进行加工，返回代理对象)
        if ("smartDog".equals(beanName)){ //1. 通过注解查看当前类是否已被代理（需要在map中映射代理关系）
            // 返回bean的代理对象
            Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    // 代理对象的方法实现
                    System.out.println("method = " + method.getName());

                    Object result = null;

                    // 假如要实现的方法是getSum()
                    if ("getSum".equals(method.getName())){// 2.通过注解查看当前类的哪个方法被代理（需要在map中映射代理关系）
                        // 执行切面类的前置通知方法
                        SmartAnimalAspect.showBeginLog(); // 3. 通过注解@Before查看当前类的此方法被切面类的哪个方法切入（需在map中映射代理关系）
                        // 执行目标方法
                        result = method.invoke(bean, args);
                        // 执行切面类的后置通知方法
                        SmartAnimalAspect.showSuccessLog(); // 4. 通过注解@AfterRetruning查看当前类的此方法被切面类的哪个方法切入（需在map中映射代理关系）
                    }else {
                        // 执行目标方法
                        result = method.invoke(bean, args);
                    }


                    return result;
                }
            });

            // 返回加工后的代理对象
            return proxyInstance;
        }

        // 不需要aop处理，则返回原生对象
        return bean;
    }
}
