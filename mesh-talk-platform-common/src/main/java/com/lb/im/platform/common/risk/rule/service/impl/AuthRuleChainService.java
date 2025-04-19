package com.lb.im.platform.common.risk.rule.service.impl;

import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.risk.enums.RuleEnum;
import com.lb.im.platform.common.risk.rule.service.RuleChainService;
import com.lb.im.platform.common.risk.rule.service.base.BaseRuleChainService;
import com.lb.im.platform.common.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户认证规则服务实现类
 * 
 * 该服务负责验证用户是否已登录，确保只有经过身份验证的用户才能访问受保护的资源。
 * 与其他规则不同，认证规则始终处于启用状态，不可通过配置禁用。
 * 
 * 技术要点：
 * 1. 使用JWT（JSON Web Token）进行用户身份验证
 * 2. 将用户会话信息存储在请求属性中，供后续处理使用
 * 3. 对非控制器方法的请求（如静态资源）不进行认证检查
 */
@Component
public class AuthRuleChainService extends BaseRuleChainService implements RuleChainService {
    private final Logger logger = LoggerFactory.getLogger(AuthRuleChainService.class);

    /**
     * 认证规则的执行顺序
     */
    @Value("${mesh.talk.rule.authRule.order}")
    private Integer authRuleOrder;

    /**
     * 执行用户认证规则检查
     * 
     * 验证用户是否已登录，并将用户会话信息存储在请求属性中。
     * 
     * 处理逻辑：
     * 1. 检查请求是否映射到控制器方法，如果不是则直接通过（如静态资源）
     * 2. 获取用户会话，如果用户未登录则抛出NO_LOGIN异常
     * 3. 将用户会话存储在请求属性中，供后续处理使用
     * 
     * @param request HTTP请求对象
     * @param handler 处理当前请求的处理器对象
     * @return HttpCode.SUCCESS表示通过认证
     * @throws IMException 当用户未登录时抛出NO_LOGIN异常
     */
    @Override
    public HttpCode execute(HttpServletRequest request, Object handler) {
        //如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return HttpCode.SUCCESS;
        }
        UserSession userSession = this.getUserSession(request);
        if (userSession == null) {
            logger.error("AuthRuleChainService|未登录，url|{}", request.getRequestURI());
            throw new IMException(HttpCode.NO_LOGIN);
        }
        request.setAttribute(IMPlatformConstants.SESSION, userSession);
        return HttpCode.SUCCESS;
    }

    /**
     * 获取规则执行顺序
     * 
     * @return 规则的执行顺序值，优先使用配置值，若未配置则使用枚举默认值
     */
    @Override
    public int getOrder() {
        return authRuleOrder == null ? RuleEnum.AUTH.getCode() : authRuleOrder;
    }

    /**
     * 获取规则服务名称
     * 
     * @return 规则服务的名称
     */
    @Override
    public String getServiceName() {
        return RuleEnum.AUTH.getMessage();
    }
}
