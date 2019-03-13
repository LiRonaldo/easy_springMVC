package com.yuxiang.li.servlet;

import com.yuxiang.li.annotation.EasyAutowired;
import com.yuxiang.li.annotation.EasyController;
import com.yuxiang.li.annotation.EasyRequestMapping;
import com.yuxiang.li.annotation.EasyService;
import com.yuxiang.li.tuil.EasyUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/***
 * @ClassName: EasyDispatcherServlet
 * @Description: servlet
 * @Auther: liyx
 */
public class EasyDispatcherServlet extends HttpServlet {

    private Properties contextConfig = new Properties();
    private List<String> classNameList = Collections.synchronizedList(new ArrayList<String>());
    private Map<String, Object> iocMap = new ConcurrentHashMap();
    private Map<String, Method> handlerMapping = new ConcurrentHashMap<String, Method>();
    private Map<String, Object> controllerMapping = new ConcurrentHashMap<String, Object>();

    @Override

    public void init(ServletConfig config) throws ServletException {
        //初始化要进行的工作
        //1.加载配置文件
        doLoadConfig(config);
        //2.扫描包
        doScan(this.contextConfig.getProperty("scanPackage").toString());
        //3.初始化扫描进来的包
        doInitClass();
        //4.依赖注入
        doAutowrid();
        //5.初始化handlerMapping
        doInitHandlerMapping();
    }

    private void doLoadConfig(ServletConfig config) {
        //此处mvc的配置文件采用
        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream(config.getInitParameter(config.getInitParameter("contextConfigLocation")));
            if (is == null) {
                System.out.println("没有找到文件");
                return;
            }
            this.contextConfig.load(is);
        } catch (IOException e) {
            System.out.println("读取配置文件报错");
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.out.println("关io报错");
                }
            }
        }
    }

    private void doScan(String scanPath) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPath.replace("\\.", "/"));
        File classFile = new File(url.getFile());
        for (File file : classFile.listFiles()) {
            if (file.isDirectory()) {
                doScan(scanPath + "." + file.getName());
            } else {
                String className = scanPath + "." + file.getName().replace(".class", "");
                this.classNameList.add(className);
            }
        }
    }

    private void doInitClass() {
        if (this.classNameList.isEmpty()) {
            return;
        }
        for (String className : this.classNameList) {
            try {
                Class<?> clazz = Class.forName(className);
                //判断是否有注解 有助解的菜初始化
                //easyController
                if (clazz.isAnnotationPresent(EasyController.class)) {
                    Object instance = clazz.newInstance();
                    //将获得有注解的类存在ioc容器内
                    //默认是首字母小心，优先使用自定义的
                    EasyController easyController = (EasyController) clazz.getAnnotation(EasyController.class);
                    String beanName = easyController.value();
                    if (beanName.length() < 1) {
                        beanName = EasyUtil.uncaptialize(clazz.getSimpleName());
                    }
                    this.iocMap.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(EasyService.class)) {
                    Object instance = clazz.newInstance();
                    EasyService easyService = (EasyService) clazz.getAnnotation(EasyService.class);
                    String beanName = easyService.value();
                    if (beanName.length() < 1) {
                        beanName = EasyUtil.uncaptialize(clazz.getSimpleName());
                    }
                    this.iocMap.put(beanName, instance);
                    //service注解面相接口的 ，我们一般用的时候是接口，所以要把子类赋值给父类
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> cls : interfaces) {
                        this.iocMap.put(cls.getName(), instance);
                    }
                }
            } catch (Exception e) {
                System.out.println("初始化扫描的类出错");
            }
        }
    }

    private void doAutowrid() {
        if (this.iocMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : this.iocMap.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                //有autowrid注解的 都要注入
                if (!field.isAnnotationPresent(EasyAutowired.class)) {
                    continue;
                }
                EasyAutowired easyAutowired = (EasyAutowired) field.getAnnotation(EasyAutowired.class);
                String beanName = easyAutowired.value();
                if (beanName.length() < 1) {
                    beanName = EasyUtil.uncaptialize(field.getType().getSimpleName());
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), this.iocMap.get(beanName));
                } catch (IllegalAccessException e) {
                    System.out.println("注入报错");
                }
            }
        }

    }

    private void doInitHandlerMapping() {
        if (this.iocMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : this.iocMap.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(EasyController.class)) {
                continue;
            }
            String baseUrl = "";
            if (clazz.isAnnotationPresent(EasyRequestMapping.class)) {
                EasyRequestMapping easyRequestMapping = (EasyRequestMapping) clazz.getAnnotation(EasyRequestMapping.class);
                baseUrl = easyRequestMapping.value();
            }
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(EasyRequestMapping.class)) {
                    EasyRequestMapping easyRequestMapping = (EasyRequestMapping) method.getAnnotation(EasyRequestMapping.class);
                    String url = easyRequestMapping.value();
                    url = baseUrl + url;
                    this.handlerMapping.put(url, method);
                    try {
                        this.controllerMapping.put(url, clazz.newInstance());
                    } catch (Exception e) {
                        System.out.println("获得handlerMapping映射报错");
                    }
                }
            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doDispatcher(req, resp);
    }

    //接受请求
    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) {
        String requestUrl = req.getRequestURI();
        String contextPath = req.getContextPath();
        requestUrl = requestUrl.replace(contextPath, "").replaceAll("/+", "/");
        if (!this.handlerMapping.containsKey(requestUrl)) {
            try {
                resp.getWriter().write("404");
            } catch (IOException e) {
                System.out.println("404");
            }
        }
        Method method = this.handlerMapping.get(requestUrl);
        if (null == method) {
            return;
        }
        Object instance = this.controllerMapping.get(requestUrl);
        Class<?>[] parameterTypes = method.getParameterTypes();
        Map<String, String[]> requestParamterMap = req.getParameterMap();
        Object[] paramterValues = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            String requestName = parameterTypes[i].getSimpleName();
            if (requestName.equals("HttpServletRequest")) {
                paramterValues[i] = req;
            }
            if (requestName.equals("HttpServletResponse")) {
                paramterValues[i] = resp;
            }
            if (requestName.equals("String")) {
                for (Map.Entry<String, String[]> param : requestParamterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue()).replace("\\[|\\]", "").replace(",\\s", ",");
                    paramterValues[i] = value;
                }
            }
            //还有很多。。。。。参数类型
        }
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                field.set(instance, this.iocMap.get(field.getName()));
                method.invoke(instance, paramterValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
