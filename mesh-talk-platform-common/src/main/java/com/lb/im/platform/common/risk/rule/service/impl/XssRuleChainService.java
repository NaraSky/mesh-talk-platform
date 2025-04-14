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

@Component
public class XssRuleChainService extends BaseRuleChainService implements RuleChainService {
    /**
     * 日志记录器
     */
    private final Logger logger = LoggerFactory.getLogger(XssRuleChainService.class);

    @Value("${mesh.talk.rule.xssRule.enabled}")
    private Boolean xssRuleEnabled;

    @Value("${mesh.talk.rule.xssRule.order}")
    private Integer xssRuleOrder;

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

    @Override
    public int getOrder() {
        // 获取规则执行顺序，优先使用配置值，若未配置则使用枚举默认值
        return xssRuleOrder == null ? RuleEnum.XSS.getCode() : xssRuleOrder;
    }

    @Override
    public String getServiceName() {
        // 返回规则服务名称，使用枚举定义的描述信息
        return RuleEnum.XSS.getMessage();
    }

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
