package com.difx.srping;

import com.difx.demo.UserServer;
import com.difx.srping.annotion.Component;

import java.lang.reflect.Proxy;

@Component("defaultBeanPostProcessor")
public class DefaultBeanPostProcessor implements BeanPostProcessor{
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前");
        if (beanName.equals("userServer")){
            ((UserServer)bean).setBeanName("李平平");
        }
        System.out.println(bean.toString());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
//        if ("userServer".equals(beanName)){
//            Proxy.newProxyInstance()
//        }
        return bean;
    }
}
