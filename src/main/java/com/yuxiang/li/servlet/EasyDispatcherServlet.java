package com.yuxiang.li.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/***
 * @ClassName: EasyDispatcherServlet
 * @Description: servlet
 * @Auther: liyx
 */
public class EasyDispatcherServlet extends HttpServlet {

    private Properties contextConfig = new Properties();
    private List<String> classNameList = Collections.synchronizedList(new ArrayList<String>());

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
        if(this.classNameList.isEmpty())
        {
            return ;
        }
    }


    private void doAutowrid() {

    }

    private void doInitHandlerMapping() {

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
    }
}
