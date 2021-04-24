package com.difx.srping;

import com.difx.srping.annotion.Component;

@Component
public interface BeanPostProcessor {

    Object postProcessBeforeInitialization(Object bean, String beanName);

    Object postProcessAfterInitialization(Object bean, String beanName);
}
