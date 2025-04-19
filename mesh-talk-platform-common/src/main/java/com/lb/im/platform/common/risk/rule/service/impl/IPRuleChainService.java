package com.lb.im.platform.common.risk.rule.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.risk.enums.RuleEnum;
import com.lb.im.platform.common.risk.rule.service.RuleChainService;
import com.lb.im.platform.common.risk.rule.service.base.BaseRuleChainService;
import com.lb.im.platform.common.risk.window.SlidingWindowLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * IP地址访问限流规则服务实现类
 * 
 * 该服务基于客户端IP地址进行访问频率限制，
 * 防止同一IP在短时间内发起过多请求，有效防御DDoS攻击和爬虫。
 * 
 * 技术要点：
 * 1. 使用滑动窗口算法实现IP限流功能
 * 2. 支持通过配置文件动态调整限流参数
 * 3. 处理多种代理环境下的真实IP获取
 */
@Component
public class IPRuleChainService extends BaseRuleChainService implements RuleChainService {
    private final Logger logger = LoggerFactory.getLogger(XssRuleChainService.class);

    /**
     * 是否启用IP限流规则
     */
    @Value("${mesh.talk.rule.ipRule.enabled}")
    private Boolean ipRuleEnabled;

    /**
     * IP限流规则的执行顺序
     */
    @Value("${mesh.talk.rule.ipRule.order}")
    private Integer ipRuleOrder;

    /**
     * 滑动窗口大小，表示在指定时间窗口内允许的最大请求数
     */
    @Value("${mesh.talk.rule.ipRule.windowsSize}")
    private Integer windowsSize;

    /**
     * 限流窗口的周期，单位毫秒
     */
    @Value("${mesh.talk.rule.ipRule.windowPeriod}")
    private Long windowPeriod;

    /**
     * 滑动窗口限流服务，用于实现限流算法
     */
    @Autowired
    private SlidingWindowLimitService slidingWindowLimitService;

    /**
     * 执行IP限流规则检查
     * 
     * 获取客户端真实IP地址，使用滑动窗口算法判断IP的访问频率是否超过限制。
     * 
     * 处理逻辑：
     * 1. 如果规则未启用，直接返回成功
     * 2. 使用默认值或配置值设置滑动窗口参数
     * 3. 获取客户端真实IP地址
     * 4. 调用滑动窗口限流服务判断是否允许通过
     * 
     * @param request HTTP请求对象
     * @param handler 处理当前请求的处理器对象
     * @return HttpCode.SUCCESS表示通过检查，HttpCode.PROGRAM_ERROR表示被限流
     */
    @Override
    public HttpCode execute(HttpServletRequest request, Object handler) {
        // 如果IP限流规则未启用，直接返回成功
        if (BooleanUtil.isFalse(ipRuleEnabled)) {
            return HttpCode.SUCCESS;
        }
        try {
            // 使用默认值或配置值设置滑动窗口参数
            windowsSize = windowsSize == null ? DEFAULT_WINDOWS_SIZE : windowsSize;
            windowPeriod = windowPeriod == null ? DEFAULT_WINDOWS_PERIOD : windowPeriod;
            // 获取客户端真实IP地址
            String ip = this.getIp(request);
            // 调用滑动窗口限流服务判断是否允许通过
            boolean result = slidingWindowLimitService.passThough(ip, windowPeriod, windowsSize);
            return result ? HttpCode.SUCCESS : HttpCode.PROGRAM_ERROR;
        } catch (Exception e) {
            logger.error("IPRuleChainService|IP限制异常|{}", e.getMessage());
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
        return ipRuleOrder == null ? RuleEnum.IP.getCode() : ipRuleOrder;
    }

    /**
     * 获取规则服务名称
     * 
     * @return 规则服务的名称
     */
    @Override
    public String getServiceName() {
        return RuleEnum.IP.getMessage();
    }
}
