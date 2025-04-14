package com.lb.im.platform.common.risk.rule.service.base;

import com.lb.im.platform.common.risk.rule.service.RuleChainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * 规则链服务的基础抽象类，提供获取服务名称、日志记录及IP地址解析功能。
 */
public abstract class BaseRuleChainService implements RuleChainService {

    /**
     * 获取当前服务的服务名称。
     *
     * @return 服务名称字符串
     */
    public abstract String getServiceName();

    private final Logger logger = LoggerFactory.getLogger(BaseRuleChainService.class);

    /**
     * 初始化规则链服务，并记录当前服务名称。
     */
    public BaseRuleChainService() {
        logger.info("IMBaseRuleChainService|当前规则服务|{}", this.getServiceName());
    }

    /**
     * 从HTTP请求中解析客户端的IP地址。
     *
     * @param request 当前的ServerHttpRequest对象
     * @return 解析后的客户端IP地址，格式为IPv4（替换可能的冒号）
     */
    protected String getIp(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        // 尝试其他HTTP头字段获取IP地址
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        // 最终使用远程地址获取IP
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress().getAddress().getHostAddress();
        }
        return ip.replaceAll(":", ".");
    }
}
