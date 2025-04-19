package com.lb.im.platform.common.risk.rule.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.risk.enums.RuleEnum;
import com.lb.im.platform.common.risk.rule.service.RuleChainService;
import com.lb.im.platform.common.risk.rule.service.base.BaseRuleChainService;
import com.lb.im.platform.common.utils.XssUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/**
 * XSS（跨站脚本）攻击防护规则服务实现类
 * 
 * 该服务检查请求中的参数和请求体是否包含XSS攻击代码，
 * 防止恶意脚本注入，保护系统和用户数据安全。
 * 
 * 技术要点：
 * 1. 使用XssUtils工具类检测XSS攻击代码
 * 2. 同时检查请求参数和请求体内容
 * 3. 支持通过配置文件动态启用/禁用XSS防护
 */
@Component
public class XssRuleChainService extends BaseRuleChainService implements RuleChainService {
    /**
     * 日志记录器
     */
    private final Logger logger = LoggerFactory.getLogger(XssRuleChainService.class);

    /**
     * 是否启用XSS防护规则
     */
    @Value("${mesh.talk.rule.xssRule.enabled}")
    private Boolean xssRuleEnabled;

    /**
     * XSS防护规则的执行顺序
     */
    @Value("${mesh.talk.rule.xssRule.order}")
    private Integer xssRuleOrder;

    /**
     * 执行XSS防护规则检查
     * 
     * 检查请求参数和请求体中是否包含XSS攻击代码，如JavaScript脚本等。
     * 
     * 处理逻辑：
     * 1. 如果规则未启用，直接返回成功
     * 2. 检查所有请求参数中是否包含XSS攻击代码
     * 3. 检查请求体中是否包含XSS攻击代码
     * 4. 如果检测到XSS攻击，返回XSS_PARAM_ERROR错误码
     * 
     * @param request HTTP请求对象
     * @param handler 处理当前请求的处理器对象
     * @return HttpCode.SUCCESS表示通过检查，HttpCode.XSS_PARAM_ERROR表示检测到XSS攻击
     */
    @Override
    public HttpCode execute(HttpServletRequest request, Object handler) {
        // 未开启XSS规则，直接通过校验
        if (BooleanUtil.isFalse(xssRuleEnabled)){
            return HttpCode.SUCCESS;
        }

        // 检查请求参数中的XSS攻击
        Map<String, String[]> paramMap =  request.getParameterMap();
        for(String[] values:paramMap.values()){
            for(String value:values){
                if(XssUtils.checkXss(value)){
                    return HttpCode.XSS_PARAM_ERROR;
                }
            }
        }

        // 检查请求体中的XSS攻击
        String body = getBody(request);
        if(XssUtils.checkXss(body)){
            return HttpCode.XSS_PARAM_ERROR;
        }

        return HttpCode.SUCCESS;
    }

    /**
     * 获取规则执行顺序
     * 
     * @return 规则的执行顺序值，优先使用配置值，若未配置则使用枚举默认值
     */
    @Override
    public int getOrder() {
        // 获取规则执行顺序，优先使用配置值，若未配置则使用枚举默认值
        return xssRuleOrder == null ? RuleEnum.XSS.getCode() : xssRuleOrder;
    }

    /**
     * 获取规则服务名称
     * 
     * @return 规则服务的名称
     */
    @Override
    public String getServiceName() {
        // 返回规则服务名称，使用枚举定义的描述信息
        return RuleEnum.XSS.getMessage();
    }

    /**
     * 获取请求体内容
     * 
     * 读取HTTP请求的请求体内容，用于XSS检查。
     * 
     * @param request HTTP请求对象
     * @return 请求体内容字符串
     */
    private String getBody(HttpServletRequest request){
        StringBuilder sb = new StringBuilder();
        try{
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }catch (IOException e){
            logger.error("XssInterceptor.getBody|获取请求体异常:{}", e.getMessage());
        }
        return sb.toString();
    }
}
