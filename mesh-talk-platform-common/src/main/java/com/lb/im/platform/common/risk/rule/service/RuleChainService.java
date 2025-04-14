package com.lb.im.platform.common.risk.rule.service;

import com.lb.im.platform.common.model.enums.HttpCode;

import javax.servlet.http.HttpServletRequest;

/**
 * IM大后端平台的规则调用链接口
 */
public interface RuleChainService {

    /**
     * 执行处理逻辑
     */
    HttpCode execute(HttpServletRequest request, Object handler);

    /**
     * 规则链中的每个规则排序
     */
    int getOrder();
}
