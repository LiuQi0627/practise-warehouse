package com.messi.snap.up.reactor.listener;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 自定义servlet异步环境下的监听器
 */
public class ServletAsyncListener implements AsyncListener {

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        //  空实现
    }

    /**
     * 模拟超时页面
     */
    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        ServletResponse response = event.getSuppliedResponse();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"success\":false}");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 模拟错误页面
     */
    @Override
    public void onError(AsyncEvent event) throws IOException {
        ServletResponse response = event.getSuppliedResponse();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"success\":false}");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
        //  空实现
    }
}
