package com.lb.im.platform.common.interceptor;

import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.interceptor.base.BaseInterceptor;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.risk.rule.service.RuleChainService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * IMInterceptor类，用于处理IM（即时通讯）请求前的规则链检查。
 * 继承自BaseInterceptor，通过调用规则链服务进行预处理校验，确保请求符合业务规则。
 * 若校验失败则抛出IMException中断请求。
 * 
 * 该类是整个规则链机制的入口点，在Spring MVC处理请求时被调用，
 * 负责按顺序执行所有规则链服务，实现安全检查、限流等功能。
 * 
 * 技术要点：
 * 1. 实现Spring MVC的HandlerInterceptor接口的preHandle方法
 * 2. 使用责任链模式顺序执行多个规则检查
 * 3. 通过异常机制中断不符合规则的请求处理
 */
@Component
public class IMInterceptor extends BaseInterceptor {

    /**
     * 执行请求前的拦截处理，校验请求是否符合规则链要求。
     * 
     * 该方法在Controller方法执行前被调用，按顺序执行所有规则链服务，
     * 如果任何规则检查失败，则抛出异常中断请求处理。
     * 
     * 处理逻辑：
     * 1. 获取排序后的规则链服务列表
     * 2. 按顺序执行每个规则链服务的execute方法
     * 3. 如果任何规则返回非SUCCESS的HttpCode，则抛出IMException异常
     * 4. 如果所有规则都通过，则返回true允许请求继续处理
     *
     * @param request  当前HTTP请求对象
     * @param response 当前HTTP响应对象
     * @param handler  处理当前请求的处理器对象
     * @return boolean 返回true表示继续后续处理，false表示中断请求
     * @throws Exception 可能抛出的异常（由IMException包装HTTP错误码）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取排序后的规则链服务列表
        List<RuleChainService> ruleChainServices = this.getRuleChainServices();
        // 遍历执行所有规则链服务的校验逻辑
        for (RuleChainService ruleChainService : ruleChainServices) {
            HttpCode httpCode = ruleChainService.execute(request, handler);
            if (httpCode != HttpCode.SUCCESS) {
                if (!HttpCode.SUCCESS.getCode().equals(httpCode.getCode())) {
                    throw new IMException(httpCode);
                }
            }
        }
        return true;
    }
}
