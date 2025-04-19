package com.lb.im.platform.message;

import com.lb.im.platform.common.threadpool.ThreadPoolUtils;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * IM平台消息服务启动类
 * 提供即时通讯平台的消息处理服务
 * 包括私聊消息、消息存储、历史记录查询等功能
 */
@EnableDubbo                     // 启用Dubbo分布式服务框架
@EnableDiscoveryClient           // 启用服务发现客户端
@EnableAspectJAutoProxy(exposeProxy = true)  // 启用AOP并暴露代理对象
@MapperScan(basePackages = {"com.lb.im.platform.message.domain.repository"})  // 配置MyBatis Mapper扫描路径
@ComponentScan(basePackages = {"com.lb.im"})  // 组件扫描路径
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})  // 排除Spring Security自动配置
public class IMPlatformMessageStarter {

    /**
     * 应用程序入口点
     * 配置JVM关闭钩子用于优雅关闭线程池
     * 设置用户主目录并启动Spring应用
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 添加JVM关闭钩子，确保应用关闭时线程池能够正确释放资源
        Runtime.getRuntime().addShutdownHook(new Thread(ThreadPoolUtils::shutdown));
        
        // 设置用户主目录路径
        System.setProperty("user.home", "C:\\soft\\code\\bh-im-platform\\bh-im-platform-message");
        
        // 启动Spring应用
        SpringApplication.run(IMPlatformMessageStarter.class, args);
    }
}