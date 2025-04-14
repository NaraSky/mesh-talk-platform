package com.lb.im.platform.common.risk.rule.service.base;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.lb.im.common.domain.jwt.JwtUtils;
import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.jwt.JwtProperties;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.enums.HttpCode;
import com.lb.im.platform.common.risk.rule.service.RuleChainService;
import com.lb.im.platform.common.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 规则链服务的基础抽象类，提供获取服务名称、日志记录及IP地址解析功能。
 */
public abstract class BaseRuleChainService implements RuleChainService {

    protected static final int DEFAULT_WINDOWS_SIZE = 50;
    protected static final int DEFAULT_WINDOWS_PERIOD = 1000;

    @Autowired
    private JwtProperties jwtProperties;

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    // 客户端与服务器同为一台机器，获取的 ip 有时候是 ipv6 格式
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String SEPARATOR = ",";

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
    protected String getIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (LOCALHOST_IP.equalsIgnoreCase(ip) || LOCALHOST_IPV6.equalsIgnoreCase(ip)) {
                // 根据网卡取本机配置的 IP
                InetAddress iNet = null;
                try {
                    iNet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    logger.error("BaseRuleChainService.getIp|获取客户端ip地址异常|{}", e.getMessage());
                }
                if (iNet != null)
                    ip = iNet.getHostAddress();
            }
        }
        // 对于通过多个代理的情况，分割出第一个 IP
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(SEPARATOR) > 0) {
                ip = ip.substring(0, ip.indexOf(SEPARATOR));
            }
        }
        return LOCALHOST_IPV6.equals(ip) ? LOCALHOST_IP : ip;
    }

    /**
     * 获取UserSession
     */
    protected UserSession getUserSession(HttpServletRequest request) {
        //从 http 请求头中取出 token
        String token = request.getHeader(IMPlatformConstants.ACCESS_TOKEN);
        if (StrUtil.isEmpty(token)) {
            logger.error("BaseRuleChainService|未登录，url|{}", request.getRequestURI());
            throw new IMException(HttpCode.NO_LOGIN);
        }
        //验证 token
        if (!JwtUtils.checkSign(token, jwtProperties.getAccessTokenSecret())) {
            logger.error("BaseRuleChainService|token已失效，url|{}", request.getRequestURI());
            throw new IMException(HttpCode.INVALID_TOKEN);
        }
        // 存放session
        String strJson = JwtUtils.getInfo(token);
        if (StrUtil.isEmpty(strJson)) {
            logger.error("BaseRuleChainService|token已失效，url|{}", request.getRequestURI());
            throw new IMException(HttpCode.INVALID_TOKEN);
        }
        return JSON.parseObject(strJson, UserSession.class);
    }

    /**
     * 获取UserSession
     */
    protected UserSession getUserSessionWithoutException(HttpServletRequest request) {
        //从 http 请求头中取出 token
        String token = request.getHeader(IMPlatformConstants.ACCESS_TOKEN);
        if (StrUtil.isEmpty(token)) {
            return null;
        }
        //验证 token
        if (!JwtUtils.checkSign(token, jwtProperties.getAccessTokenSecret())) {
            return null;
        }
        // 存放session
        String strJson = JwtUtils.getInfo(token);
        if (StrUtil.isEmpty(strJson)) {
            return null;
        }
        return JSON.parseObject(strJson, UserSession.class);
    }

}
