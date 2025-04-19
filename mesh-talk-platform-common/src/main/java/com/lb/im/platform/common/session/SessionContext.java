package com.lb.im.platform.common.session;

import com.lb.im.platform.common.exception.IMException;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.common.model.enums.HttpCode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 提供获取当前请求用户会话的工具方法。
 */
public class SessionContext {

    /**
     * 获取当前请求中的用户会话对象。
     *
     * @return 当前请求中的UserSession对象，存储在请求属性中，键为 {@link IMPlatformConstants#SESSION}
     */
    public static UserSession getSession() {
        /* 从Spring的请求上下文中获取当前的ServletRequestAttributes对象 */
        ServletRequestAttributes requestAttributes = ServletRequestAttributes.class.cast(RequestContextHolder.getRequestAttributes());
        /* 从ServletRequestAttributes中获取HttpServletRequest对象 */
        HttpServletRequest request = requestAttributes.getRequest();
        Object object = request.getAttribute(IMPlatformConstants.SESSION);
        if (object == null) {
            throw new IMException(HttpCode.NO_LOGIN);
        }
        return (UserSession) object;
    }
}
