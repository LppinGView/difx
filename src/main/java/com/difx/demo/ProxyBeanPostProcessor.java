package com.difx.demo;

import com.difx.srping.BeanPostProcessor;
import com.difx.srping.annotion.Component;
import com.difx.srping.exception.BeansException;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component("proxyBeanPostProcessor")
public class ProxyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return null;
    }

    /**
     * 代理方法
     * @param bean
     * @param beanName
     * @return
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    public Object getEarlyBeanReference(String beanName, Object bean) throws BeansException {
        if ("userServer".equals(beanName)){
            System.out.println("--------------");
            System.out.println(bean);
            Object proxyInstance = Proxy.newProxyInstance(bean.getClass().getClassLoader(), new Class[]{UserServer.class}, (Object proxy, Method method, Object[] args) -> {
                //先执行代理方法
                System.out.println("---------代理方法--------");
                return method.invoke(bean, args);
            });
            return proxyInstance;
        }
        return bean;
    }
}
