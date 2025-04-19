package com.lb.im.platform.common.risk.rule.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.risk.enums.RuleEnum;
import com.lb.im.platform.common.risk.rule.service.RuleChainService;
import com.lb.im.platform.common.risk.rule.service.base.BaseRuleChainService;
import com.lb.im.platform.common.risk.window.SlidingWindowLimitService;
import com.lb.im.platform.common.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 路径访问限流规则服务实现类
 * 
 * 该服务基于用户ID、终端和请求路径的组合进行访问频率限制，
 * 防止用户在短时间内对同一资源路径发起过多请求，保护系统资源。
 * 
 * 技术要点：
 * 1. 使用滑动窗口算法实现限流功能
 * 2. 支持通过配置文件动态调整限流参数
 * 3. 对未登录用户不进行限流处理
 */
@Component
public class PathRuleChainService extends BaseRuleChainService implements RuleChainService {

    private final Logger logger = LoggerFactory.getLogger(PathRuleChainService.class);

    /**
     * 是否启用路径限流规则
     */
    @Value("${mesh.talk.rule.pathRule.enabled}")
    private Boolean pathRuleEnabled;

    /**
     * 路径限流规则的执行顺序
     */
    @Value("${mesh.talk.rule.pathRule.order}")
    private Integer pathRuleOrder;

    /**
     * 滑动窗口大小，表示在指定时间窗口内允许的最大请求数
     */
    @Value("${mesh.talk.rule.pathRule.windowsSize}")
    private Integer windowsSize;

    /**
     * 限流窗口的周期，单位毫秒
     */
    @Value("${mesh.talk.rule.pathRule.windowPeriod}")
    private Long windowPeriod;

    /**
     * 滑动窗口限流服务，用于实现限流算法
     */
    @Autowired
    private SlidingWindowLimitService slidingWindowLimitService;

    /**
     * 执行路径限流规则检查
     * 
     * 根据用户ID、终端和请求路径的组合创建唯一标识，
     * 使用滑动窗口算法判断用户对特定路径的访问频率是否超过限制。
     * 
     * 处理逻辑：
     * 1. 如果规则未启用，直接返回成功
     * 2. 获取用户会话，如果用户未登录则不进行限流（返回成功）
     * 3. 使用请求URI、用户ID和终端组合生成唯一标识
     * 4. 调用滑动窗口限流服务判断是否允许通过
     * 
     * @param request HTTP请求对象
     * @param handler 处理当前请求的处理器对象
     * @return HttpCode.SUCCESS表示通过检查，HttpCode.PROGRAM_ERROR表示被限流
     */
    @Override
    public HttpCode execute(HttpServletRequest request, Object handler) {
        // 如果路径限流规则未启用，直接返回成功
        if (BooleanUtil.isFalse(pathRuleEnabled)) {
            return HttpCode.SUCCESS;
        }
        try {
            // 获取用户会话，不抛出异常
            UserSession userSession = this.getUserSessionWithoutException(request);
            // 如果用户未登录，不进行限流
            if (userSession == null) {
                return HttpCode.SUCCESS;
            }
            // 使用默认值或配置值设置滑动窗口参数
            windowsSize = windowsSize == null ? DEFAULT_WINDOWS_SIZE : windowsSize;
            windowPeriod = windowPeriod == null ? DEFAULT_WINDOWS_PERIOD : windowPeriod;
            // 生成用户路径唯一标识：请求URI + 用户ID + 终端
            String userPath = request.getRequestURI().concat(String.valueOf(userSession.getUserId())).concat(String.valueOf(userSession.getTerminal()));
            // 调用滑动窗口限流服务判断是否允许通过
            boolean result = slidingWindowLimitService.passThough(userPath, windowPeriod, windowsSize);
            return result ? HttpCode.SUCCESS : HttpCode.PROGRAM_ERROR;
        } catch (Exception e) {
            logger.error("PathRuleChainService|资源限制异常|{}", e.getMessage());
            return HttpCode.PROGRAM_ERROR;
        }
    }

    /**
     * 获取规则执行顺序
     * 
     * @return 规则的执行顺序值，优先使用配置值，若未配置则使用枚举默认值
     */
    @Override
    public int getOrder() {
        return pathRuleOrder == null ? RuleEnum.PATH.getCode() : pathRuleOrder;
    }

    /**
     * 获取规则服务名称
     * 
     * @return 规则服务的名称
     */
    @Override
    public String getServiceName() {
        return RuleEnum.PATH.getMessage();
    }
}
