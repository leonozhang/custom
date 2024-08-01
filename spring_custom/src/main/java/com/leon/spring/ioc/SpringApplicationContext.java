package com.leon.spring.ioc;

import com.leon.spring.annotation.Autowired;
import com.leon.spring.annotation.Component;
import com.leon.spring.annotation.ComponentScan;
import com.leon.spring.annotation.Scope;
import com.leon.spring.processor.BeanPostProcessor;
import com.leon.spring.processor.InitializingBean;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SpringApplicationContext {
    // 配置类
    private final Class<?> configClass;

    // 存放BeanDefinition对象
    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    // 存放单例对象
    private final ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();
    // 存放后置处理器
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public SpringApplicationContext(Class<?> configClass) {
        this.configClass = configClass;

        // 完成对指定包的扫描，并将Bean信息封装到BeanDefinition对象，放入到容器
        beanDefinitionByScan();

        // 初始化单例池
        Enumeration<String> keys = beanDefinitionMap.keys();
        while (keys.hasMoreElements()) {
            String beanName = keys.nextElement();
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if ("singleton".equals(beanDefinition.getScope())) {
                // 将ban实例添加至singletonObjects中
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    // 完成对包的扫描
    private void beanDefinitionByScan() {
        // 1. 根据配置类，获取要扫描的包路径
        // a.获取配置类的ComponentScan注解对象
        ComponentScan componentScan = (ComponentScan) this.configClass.getAnnotation(ComponentScan.class);
        // b.通过注解获取其配置的扫描包路径
        String path = componentScan.value();
        // System.out.println("要扫描的包 = " + path);

        // 2. 扫描包下所有资源
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        URL resource = loader.getResource(path.replace(".", "/"));
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                String fileAbsolutePath = f.getAbsolutePath();
                String className = fileAbsolutePath.substring(fileAbsolutePath.lastIndexOf("\\") + 1, fileAbsolutePath.indexOf(".class"));
                String classFullName = path.replace("/", ".") + "." + className;

                try {
                    // 判断该类是否为要注入容器的组件
                    Class<?> aClass = loader.loadClass(classFullName);
                    if (aClass.isAnnotationPresent(Component.class)) {
                        // 将后置处理器添加至容器中
                        // 不能使用instanceof判断，aClass不是一个实例对象
                        if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
                            BeanPostProcessor beanPostProcessor = (BeanPostProcessor) aClass.newInstance();
                            beanPostProcessors.add(beanPostProcessor);
                        }

                        // 获取类名
                        Component component = aClass.getDeclaredAnnotation(Component.class);
                        String beanName = component.value();      // 配置的类名
                        if ("".equals(beanName)) {
                            // beanName = className.substring(0,1).toLowerCase() + className.substring(1);
                            beanName = StringUtils.uncapitalize(className);
                        }

                        // 根据全类名获取其反射类
                        Class<?> clazz = Class.forName(classFullName);

                        // 没有配置，默认为singleton
                        String scope = "singleton";

                        // 获取scope值
                        if (clazz.isAnnotationPresent(Scope.class)) {
                            String scopeValue = clazz.getDeclaredAnnotation(Scope.class).value();
                            scope = scopeValue;
                        }

                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setClazz(clazz);
                        beanDefinition.setScope(scope);

                        // 将beanDefinition放入到容器中
                        beanDefinitionMap.put(beanName, beanDefinition);
                    } else {
                        System.out.println(aClass + "不是一个spring bean");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // 根据BeanDefinition创建Bean实例
    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        // 获取bean的class对象
        Class<?> clazz = beanDefinition.getClazz();

        // 使用反射获取实例
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 依赖注入
            // 遍历当前创建对象的所有字段，判断是否有Autowired注解声明
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    // 字段名
                    String fieldName = field.getName();

                    // 通过字段名，从容器中获取要匹配的对象实例
                    Object bean = getBean(fieldName);

                    // 反射爆破赋值
                    field.setAccessible(true);
                    field.set(instance, bean);
                }
            }
            // System.out.println("=============创建实例" + instance + "=============");
            // 在Bean的初始化方法前调用后置处理器的Before方法
            // 对Bean实例进行处理，返回处理后的bean
            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                Object current = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
                if (current != null) {
                    instance = current;
                }
            }

            // 判断是否执行bean的初始化方法
            if (instance instanceof InitializingBean){
                ((InitializingBean) instance).afterPropertiesSet();
            }

            // 在Bean的初始化方法前调用后置处理器的Before方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                Object current = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
                if (current != null) {
                    instance = current;
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 根据beanName获取对象实例
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        if (beanDefinitionMap.containsKey(beanName)) {
            // 单例对象，从单例池获取
            if ("singleton".equals(beanDefinition.getScope())) {
                return singletonObjects.get(beanName);
            }

            // 非单例，则调用createBean反射创建一个对象
            return createBean(beanName, beanDefinition);
        }

        throw new NullPointerException("该bean不存在: " + beanName);
    }
}
