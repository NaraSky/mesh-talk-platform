package com.lb.im.platform.common.risk.rule.service;

import com.lb.im.platform.common.model.enums.HttpCode;

import javax.servlet.http.HttpServletRequest;

/**
 * IM大后端平台的规则调用链接口
 * 
 * 该接口定义了规则链模式中的规则处理组件，用于实现请求的安全检查、限流等功能。
 * 系统会按照规则的优先级顺序依次执行所有规则，任一规则检查失败则终止请求处理。
 * 
 * 技术要点：
 * 1. 采用责任链模式设计，将请求处理逻辑解耦为多个独立规则
 * 2. 结合Spring的依赖注入机制自动收集和排序所有规则实现
 * 3. 通过返回HttpCode枚举值表示规则检查结果
 */
public interface RuleChainService {

    /**
     * 执行处理逻辑
     * 
     * 对请求执行具体的规则检查逻辑，如XSS防护、IP限流、路径限流、认证检查等。
     * 
     * @param request HTTP请求对象，包含请求的所有信息
     * @param handler 处理当前请求的处理器对象，通常是Controller方法
     * @return HttpCode 返回处理结果状态码，SUCCESS表示通过检查，其他值表示检查失败
     */
    HttpCode execute(HttpServletRequest request, Object handler);

    /**
     * 规则链中的每个规则排序
     * 
     * 返回当前规则在规则链中的执行顺序，数值越小优先级越高。
     * 系统会根据此值对所有规则进行排序，按顺序执行。
     * 
     * @return int 规则的执行顺序值
     */
    int getOrder();
}
