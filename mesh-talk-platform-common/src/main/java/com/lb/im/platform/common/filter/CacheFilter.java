package com.lb.im.platform.common.filter;

import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 缓存过滤器，用于包装HttpServletRequest以便后续处理可多次访问请求体
 */
@Component
@ServletComponentScan // 扫描Servlet组件
@WebFilter(urlPatterns = "/*", filterName = "xssFilter") // 拦截所有URL路径
public class CacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        // 创建请求包装器实例，将原始请求包装为可缓存的版本
        filterChain.doFilter(
                new CacheHttpServletRequestWrapper((HttpServletRequest) servletRequest),
                servletResponse
        );
    }
}