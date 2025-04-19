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
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;

/**
 * 规则链服务的基础抽象类，实现了RuleChainService接口的通用功能
 * 
 * 该类提供了所有规则实现类共用的基础功能，包括：
 * 1. 获取客户端真实IP地址
 * 2. 用户会话管理和JWT令牌验证
 * 3. 默认的滑动窗口参数（用于限流）
 * 
 * 技术要点：
 * 1. 使用JWT（JSON Web Token）进行用户认证
 * 2. 处理多种代理环境下的客户端IP获取
 * 3. 提供异常处理和无异常处理两种用户会话获取方式
 */
public abstract class BaseRuleChainService implements RuleChainService {

    private final Logger logger = LoggerFactory.getLogger(BaseRuleChainService.class);

    /**
     * 默认滑动窗口大小（用于限流规则）
     */
    protected static final int DEFAULT_WINDOWS_SIZE = 50;

    /**
     * 默认滑动窗口周期，单位毫秒（用于限流规则）
     */
    protected static final int DEFAULT_WINDOWS_PERIOD = 1000;

    @Autowired
    private JwtProperties jwtProperties;

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    // 客户端与服务器同为一台机器，获取的 ip 有时候是 ipv6 格式
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String SEPARATOR = ",";

    /**
     * 构造函数，记录当前规则服务的名称
     */
    public BaseRuleChainService() {
        logger.info("IMBaseRuleChainService|当前规则服务|{}", this.getServiceName());
    }

    /**
     * 获取客户端真实IP地址
     * 
     * 该方法处理了各种代理环境下的IP获取逻辑，按以下顺序尝试获取：
     * 1. x-forwarded-for头
     * 2. Proxy-Client-IP头
     * 3. X-Forwarded-For头
     * 4. WL-Proxy-Client-IP头
     * 5. X-Real-IP头
     * 6. 请求的远程地址
     * 
     * 对于本地请求（127.0.0.1或IPv6格式的本地地址），尝试获取本机网卡IP
     * 对于多级代理的情况，提取第一个非unknown的IP地址
     * 
     * @param request HTTP请求对象
     * @return 客户端真实IP地址，如果无法获取则返回"unknown"
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
     * 获取用户会话（带异常处理）
     * 
     * 从请求头中获取访问令牌，验证其有效性，并解析出用户会话信息。
     * 如果令牌不存在或无效，将抛出IMException异常。
     * 
     * @param request HTTP请求对象
     * @return 用户会话对象
     * @throws IMException 当用户未登录或令牌无效时抛出
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
     * 获取用户会话（无异常处理）
     * 
     * 从请求头中获取访问令牌，验证其有效性，并解析出用户会话信息。
     * 与getUserSession不同，此方法在令牌不存在或无效时返回null而不是抛出异常。
     * 适用于不强制要求用户登录的场景。
     * 
     * @param request HTTP请求对象
     * @return 用户会话对象，如果未登录或令牌无效则返回null
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

    /**
     * 获取当前规则服务的名称
     * 
     * 由子类实现，用于日志记录和服务识别
     * 
     * @return 规则服务的名称
     */
    public abstract String getServiceName();
}
