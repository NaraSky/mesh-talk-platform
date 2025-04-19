package com.lb.im.platform.group;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableDubbo
@EnableDiscoveryClient
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan(basePackages = {"com.lb.im.platform.group.domain.repository"})
@ComponentScan(basePackages = {"com.lb.im"})
@SpringBootApplication(exclude= {SecurityAutoConfiguration.class })// 禁用secrity
public class IMPlatformGroupStarter {

    public static void main(String[] args) {
        SpringApplication.run(IMPlatformGroupStarter.class, args);
    }
}
