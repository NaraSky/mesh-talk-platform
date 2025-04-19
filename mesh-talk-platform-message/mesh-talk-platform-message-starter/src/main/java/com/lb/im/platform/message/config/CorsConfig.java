package com.lb.im.platform.message.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * 跨域请求配置类
 * 用于处理前端与后端API的跨域资源共享(CORS)问题
 * 允许来自不同源的HTTP请求访问API资源
 */
@Configuration
public class CorsConfig {

    /**
     * 配置CORS过滤器
     * 设置允许的源、头信息、请求方法等CORS策略
     *
     * @return 配置好的CORS过滤器注册Bean
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> corsFilterFilterRegistrationBean = new FilterRegistrationBean<>();
        
        //添加CORS配置信息
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        
        //允许的域，不要写*，否则cookie就无法使用了
        //当前设置为*表示允许所有域，生产环境应该限制为特定域名
        corsConfiguration.addAllowedOrigin("*");
        
        //允许的头信息，*表示允许所有头
        corsConfiguration.addAllowedHeader("*");
        
        //允许的HTTP请求方法
        corsConfiguration.setAllowedMethods(Arrays.asList("POST", "PUT", "GET", "OPTIONS", "DELETE"));
        
        //是否发送cookie信息
        corsConfiguration.setAllowCredentials(true);
        
        //预检请求的有效期，单位为秒
        corsConfiguration.setMaxAge(3600L);

        //添加映射路径，标识待拦截的请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        
        //创建并配置过滤器
        corsFilterFilterRegistrationBean.setFilter(new CorsFilter(source));
        
        //设置过滤器优先级为最高
        corsFilterFilterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        
        return corsFilterFilterRegistrationBean;
    }
}