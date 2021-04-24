package com.difx.demo;

import com.difx.config.AppConfig;
import com.difx.srping.DiFxApplication;

public class DiFxTest {

    public static void main(String[] args) {
        DiFxApplication context = new DiFxApplication(AppConfig.class);
        UserServer bean = (UserServer) context.getBean("userServer");
        bean.test();
    }
}
