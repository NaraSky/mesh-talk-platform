package com.lb.im.platform.config;

import com.lb.im.platform.common.interceptor.IMInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC配置类，用于注册拦截器和密码编码器。
 * 实现WebMvcConfigurer接口以自定义Web MVC行为，包括拦截器路径配置和密码加密策略。
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Autowired
    private IMInterceptor imInterceptor;

    /**
     * 配置全局拦截器，将IMInterceptor应用到所有路径，同时排除指定的公开端点。
     *
     * @param registry 拦截器注册表，用于管理拦截器的注册和路径匹配规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 配置拦截器排除特定路径
        registry.addInterceptor(imInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login","/logout","/register","/refreshToken",
                                     "/swagger-resources/**", "/webjars/**", "/v2/**", "/swagger-ui.html/**");
    }

    /**
     * 提供密码编码器Bean，使用BCrypt算法对密码进行加密和验证。
     *
     * @return BCryptPasswordEncoder实例，实现PasswordEncoder接口
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        // 使用BCrypt加密密码
        return new BCryptPasswordEncoder();
    }
}
