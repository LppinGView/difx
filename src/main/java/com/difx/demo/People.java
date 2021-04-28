package com.difx.demo;

import com.difx.srping.annotion.Autowired;
import com.difx.srping.annotion.Component;

@Component("people")
public class People {

    @Autowired
    private ProxyBeanPostProcessor proxyBeanPostProcessor;

    @Autowired
    private DefaultBeanPostProcessor defaultBeanPostProcessor;

    @Autowired
    private UserServer userServer;

//    @Override
//    public String toString() {
//        return "People{" +
//                "proxyBeanPostProcessor=" + proxyBeanPostProcessor +
//                ", defaultBeanPostProcessor=" + defaultBeanPostProcessor +
//                ", userServer=" + userServer +
//                '}';
//    }
}
