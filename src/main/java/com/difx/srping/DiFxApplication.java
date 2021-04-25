package com.difx.srping;

import com.difx.srping.annotion.Autowired;
import com.difx.srping.annotion.Component;
import com.difx.srping.annotion.ComponentScan;
import com.difx.srping.annotion.Scope;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.difx.srping.enums.ScopeEnum.PROTOTYPE;
import static com.difx.srping.enums.ScopeEnum.SINGLETON;

public class DiFxApplication {

    private final ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>(); //单例池
    private final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    private Class configClazz;

    public DiFxApplication(Class clazz) {
        this.configClazz = clazz;

        //1.通过配置类的扫描路径，加载所有bean，生成相应的beanDefinition
        scan(clazz);

        //遍历beanDefinitionMap 处理aware回调，bean初始化，beanPostProcessor等流程，aop
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()){
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (SINGLETON == beanDefinition.getScope()){
                Object bean = createBean(beanDefinition);
                singletonObjects.put(beanName, bean);
            }

            Object bean = singletonObjects.get(beanName);
            if (bean instanceof BeanNameAware){
                ((BeanNameAware) bean).setBeanName(beanName);
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessBeforeInitialization(bean, beanName);
            }

            //bean初始化...
            System.out.println("bean初始化...");

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                beanPostProcessor.postProcessAfterInitialization(bean, beanName);
            }

        }


    }

    /**
     * Desc: @ComponentScan --> 获得扫描路径 --> 扫描路径下带有@Component注解的类，
     * 生成BeanDefinition --> BeanDefinitionMap
     *
     * 加载配置类，获取需要被fx扫描的相对类路径
     * 类的加载器
     * BootstrapClassLoader     ----> jre/lib/rt.jar
     * ExtClassLoader           ----> jre/ext/lib
     * ApplicationClassLoader   ----> classpath
     *
     * @param clazz 配置类
     * @return
     * @author 278019309@qq.com
     * @date 2021/4/24 22:44
     */
    private void scan(Class clazz) {
        ClassLoader classLoader = clazz.getClassLoader(); //ApplicationClassLoader
        URL scanResource = getScanUrl(clazz, classLoader);
        File file = new File(scanResource.getFile());
        if (file.isDirectory()) {
            //获取当前文件夹下所有文件
            File[] files = file.listFiles();
            for (File f : files) {
                System.out.println(f);
                //截取当前类的全限定名，用于加载类
                String classPath = getClassPath(f.getAbsolutePath());
                System.out.println(classPath);
                try {
                    //只初始化组件类
                    Class clz = classLoader.loadClass(classPath);
                    if (clz.isAnnotationPresent(Component.class)){
                        Component annotation = (Component) clz.getDeclaredAnnotation(Component.class);
                        String beanName = annotation.value();
                        //创建类的beanDefinition
                        createBeanDefinition(clz, beanName);
                    }

                }catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //通过类型和
    private void createBeanDefinition(Class clz, String beanName) {
        //创建beanDefinition 该类是否为单例、懒加载等等
        BeanDefinition beanDefinition = new BeanDefinition();
        Scope scope = (Scope) clz.getDeclaredAnnotation(Scope.class);
        if (null != scope && (PROTOTYPE == (scope.value()))){
            beanDefinition.setScope(PROTOTYPE);
        }else {
            beanDefinition.setScope(SINGLETON);
        }
        beanDefinition.setClazz(clz);
        beanDefinitionMap.put(beanName, beanDefinition);
    }

    //注入依赖的bean
    private void autowriedBean(Class clz, Object instance) throws IllegalAccessException {
        //对Autowired注解元素进行赋值
        for (Field field: clz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)){
                Object autowiredBean = getBean(field.getName());
                if (null == autowiredBean){
                    autowiredBean = createBean(beanDefinitionMap.get(field.getName()));
                }
                field.setAccessible(true);
                field.set(instance, autowiredBean);
            }
        }
    }

    //通过类型，和类加载器，获取注解上扫描路径
    private URL getScanUrl(Class clazz, ClassLoader classLoader) {
        ComponentScan scan = (ComponentScan) clazz.getDeclaredAnnotation(ComponentScan.class);
        String scanPath = scan.value().replace(".", "/");
        URL resource = classLoader.getResource(scanPath);
        return resource;
    }

    /**
     * 通过文件地址获取类的全限定名
     * @param addr
     * @return
     */
    private String getClassPath(String addr){
        return addr.substring(addr.indexOf("com"),
                addr.indexOf(".class")).replace("/", ".");
    }

    /***
     * Desc: 通过BeanDefinition，使用反射机制，调用类的无参构造获取bean实例
     * @param beanDefinition
     * @return {@link Object}
     * @author 278019309@qq.com
     * @date 2021/4/24 22:51
     */
    private Object createBean(BeanDefinition beanDefinition){
        try {

            Class clazz = beanDefinition.getClazz();
            Object instance = clazz.getDeclaredConstructor().newInstance();

            //注入依赖bean
            autowriedBean(clazz, instance);

            //装载BeanPostProcessor 这里是放入一个list 可以将其优化为BeanDefinition
            if (BeanPostProcessor.class.isAssignableFrom(clazz)){
                beanPostProcessorList.add((BeanPostProcessor) instance);
            }
            return instance;

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Desc: 通过beanName，获取bean
     * @param beanName
     * @return {@link Object}
     * @author 278019309@qq.com
     * @date 2021/4/24 22:50
     */
    public Object getBean(String beanName){
        if (beanDefinitionMap.containsKey(beanName)){
            //根据bean的beanDefinition 进行不同的类加载方式
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (SINGLETON == beanDefinition.getScope()){
                return singletonObjects.get(beanName);
            }else {
                return createBean(beanDefinition);
            }
        }else {
            throw new NullPointerException("没有该bean的类定义");
        }
    }
}
