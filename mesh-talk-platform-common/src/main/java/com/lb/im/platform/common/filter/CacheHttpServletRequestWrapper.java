package com.lb.im.platform.common.filter;

import org.apache.tomcat.util.http.fileupload.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * 包装HttpServletRequest以缓存请求体，允许多次读取输入流。
 * 通过覆盖getInputStream和getReader方法，确保请求体在首次读取时被缓存，后续读取使用缓存数据。
 * 
 * 在标准Servlet规范中，请求体只能被读取一次，这个包装器通过缓存机制解决了这一限制。
 * 适用于多个组件需要访问请求体数据的场景，如日志记录、请求验证、实际业务处理等。
 */
public class CacheHttpServletRequestWrapper extends HttpServletRequestWrapper {

    // 存储请求体内容的字节数组，懒加载方式初始化
    private byte[] requestBody;
    // 保存原始HttpServletRequest的引用
    private final HttpServletRequest request;

    /**
     * 创建CacheHttpServletRequestWrapper实例。
     * 
     * 构造函数接收原始请求对象并将其传递给父类构造函数，同时保存一个引用以便后续使用。
     *
     * @param request 被包装的原始HttpServletRequest对象
     */
    public CacheHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.request = request;
    }

    /**
     * 重写getInputStream方法，提供可重复读取的输入流。
     * 
     * 核心实现原理：
     * 1. 首次调用时，读取并缓存整个请求体
     * 2. 后续调用时，基于缓存数据创建新的输入流
     * 3. 返回自定义的ServletInputStream实现，从缓存读取数据
     *
     * @return 基于缓存数据的ServletInputStream实例
     * @throws IOException 如果读取请求输入流时发生I/O错误
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 检查是否已缓存请求体，如未缓存则读取原始请求流
        if (null == this.requestBody) {
            // 创建输出流用于保存请求体数据
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 将原始请求输入流的内容复制到输出流中
            IOUtils.copy(request.getInputStream(), baos);
            // 保存请求体内容到字节数组
            this.requestBody = baos.toByteArray();
        }
        
        // 基于缓存的请求体创建一个ByteArrayInputStream
        ByteArrayInputStream bais = new ByteArrayInputStream(requestBody);
        
        // 返回自定义的ServletInputStream实现
        return new ServletInputStream() {
            /**
             * 检查流是否已完成读取
             * 在这个实现中总是返回false，因为我们使用的是内存中的字节数组
             */
            @Override
            public boolean isFinished() {
                return false;
            }

            /**
             * 检查流是否准备好被读取
             * 在这个实现中总是返回false，表示不支持非阻塞I/O
             */
            @Override
            public boolean isReady() {
                return false;
            }

            /**
             * 设置读取监听器，用于非阻塞I/O
             * 在这个实现中是空方法，不支持非阻塞I/O
             */
            @Override
            public void setReadListener(ReadListener listener) {
                // 不支持非阻塞I/O，因此此方法为空实现
            }

            /**
             * 从底层ByteArrayInputStream读取下一个字节
             * 
             * @return 读取的字节值，如果到达流末尾则返回-1
             */
            @Override
            public int read() {
                return bais.read();
            }
        };
    }

    /**
     * 重写getReader方法，提供可重复读取的字符流。
     * 
     * 该方法基于getInputStream()实现，确保请求体已被缓存，
     * 然后创建一个字符流Reader来读取缓存的数据。
     *
     * @return 用于读取请求体的BufferedReader实例
     * @throws IOException 如果创建Reader时发生I/O错误
     */
    @Override
    public BufferedReader getReader() throws IOException {
        // 基于缓存的输入流创建字符读取器
        // 这里利用了getInputStream()中的缓存机制
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }
}