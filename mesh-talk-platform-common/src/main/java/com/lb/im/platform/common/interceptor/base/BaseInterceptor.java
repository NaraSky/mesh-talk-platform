package com.lb.im.platform.common.interceptor.base;

import cn.hutool.core.collection.CollectionUtil;
import com.lb.im.platform.common.risk.rule.service.RuleChainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础拦截器，实现了Spring的HandlerInterceptor接口，用于处理请求前的规则链服务。
 * 提供对规则链服务的排序功能，确保按预设顺序执行。
 * 
 * 该类是所有拦截器的基类，通过Spring的依赖注入机制自动收集所有RuleChainService实现，
 * 并提供获取已排序规则链的方法，供子类在拦截请求时使用。
 * 
 * 技术要点：
 * 1. 使用Spring的依赖注入自动收集所有RuleChainService实现
 * 2. 利用Java 8 Stream API对规则链进行排序
 * 3. 遵循Spring MVC的HandlerInterceptor接口规范
 */
public abstract class BaseInterceptor implements HandlerInterceptor {

    /**
     * 自动注入所有RuleChainService实现类
     * Spring会自动收集所有实现了RuleChainService接口的Bean
     */
    @Autowired
    private List<RuleChainService> ruleChainServices;

    /**
     * 获取并返回已排序的规则链服务列表。
     * 
     * 该方法按照规则链的order属性进行升序排序，确保规则按照预定义的顺序执行。
     * 子类可以调用此方法获取排序后的规则链，然后按顺序执行各个规则。
     *
     * @return 按规则链服务order属性升序排列的列表，若原列表为空则返回空列表。
     */
    public List<RuleChainService> getRuleChainServices() {
        if (CollectionUtil.isEmpty(ruleChainServices)) {
            return Collections.emptyList();
        }

        // 按规则链服务的order属性进行排序
        return ruleChainServices.stream()
                .sorted(Comparator.comparing(RuleChainService::getOrder))
                .collect(Collectors.toList());
    }
}
