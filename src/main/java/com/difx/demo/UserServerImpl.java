package com.difx.demo;

import com.difx.srping.BeanNameAware;
import com.difx.srping.BeanPostProcessor;
import com.difx.srping.annotion.Autowired;
import com.difx.srping.annotion.Component;
import com.difx.srping.annotion.Scope;

import java.lang.reflect.Proxy;

import static com.difx.srping.enums.ScopeEnum.PROTOTYPE;

@Component("userServer")
//@Scope(PROTOTYPE)
public class UserServerImpl implements UserServer {

    @Autowired
    private People people;

    private String beanName;

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

//    @Override
//    public void setBeanName(String name) {
//        this.beanName = name;
//    }

    public String toString(){
        return "userServer={people: " + people + ", beanName: " + beanName + " }";
    }

    @Override
    public void test() {
        System.out.println(people);
    }
}
