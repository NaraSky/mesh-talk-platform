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
 */
@Component
public class IMInterceptor extends BaseInterceptor {

    /**
     * 执行请求前的拦截处理，校验请求是否符合规则链要求。
     *
     * @param request  当前HTTP请求对象
     * @param response 当前HTTP响应对象
     * @param handler  处理当前请求的处理器对象
     * @return boolean 返回true表示继续后续处理，false表示中断请求
     * @throws Exception 可能抛出的异常（由IMException包装HTTP错误码）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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
