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
 */
public abstract class BaseInterceptor implements HandlerInterceptor {

    @Autowired
    private List<RuleChainService> ruleChainServices;

    /**
     * 获取并返回已排序的规则链服务列表。
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
