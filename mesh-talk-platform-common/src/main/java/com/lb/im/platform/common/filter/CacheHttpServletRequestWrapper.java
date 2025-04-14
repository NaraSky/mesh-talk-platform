package com.lb.im.platform.common.filter;

import org.apache.commons.compress.utils.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * 包装HttpServletRequest以缓存请求体，允许多次读取输入流。
 * 通过覆盖getInputStream和getReader方法，确保请求体在首次读取时被缓存，后续读取使用缓存数据。
 */
public class CacheHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private byte[] requestBody;
    private final HttpServletRequest request;

    /**
     * 创建CacheHttpServletRequestWrapper实例。
     * @param request 被包装的原始HttpServletRequest对象
     */
    public CacheHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.request = request;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 如果请求体未缓存，则读取原始输入流并缓存到requestBody数组中
        if (null == this.requestBody) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(request.getInputStream(), baos);
            this.requestBody = baos.toByteArray();
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(requestBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() {
                return bais.read();
            }
        };
    }

    /**
     * 获取请求的输入流，首次调用时缓存请求体到内部字节数组。
     * @return 包含缓存请求体的ServletInputStream
     */

    @Override
    public BufferedReader getReader() throws IOException {
        // 基于缓存的输入流创建字符读取器
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

}
