package com.yuxiang.li.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/***
 * @ClassName: EasyDispatcherServlet
 * @Description: servlet
 * @Auther: liyx
 */
public class EasyDispatcherServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化要进行的工作
        //1.加载配置文件
        doLoadConfig(config);
        //2.扫描包
        doScan();
        //3.初始化扫描进来的包
        doInitClass();
        //4.依赖注入
        doAutowrid();
        //5.初始化handlerMapping
        doInitHandlerMapping();
    }

    private void doLoadConfig(ServletConfig config) {

    }

    private void doInitClass() {
    }

    private void doScan() {
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
