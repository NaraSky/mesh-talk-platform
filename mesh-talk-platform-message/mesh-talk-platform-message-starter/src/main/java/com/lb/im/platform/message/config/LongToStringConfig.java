package com.lb.im.platform.message.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Long类型序列化配置类
 * 用于解决前端JavaScript处理Long型数值精度丢失问题
 * 将Long类型自动转换为String类型返回给前端
 */
@Configuration
public class LongToStringConfig {
    
    /**
     * 配置Jackson的ObjectMapper，添加Long类型到String的序列化转换
     * 避免JavaScript处理大数值时的精度丢失问题
     *
     * @param mapperBuilder Jackson对象映射构建器
     * @return 配置好的ObjectMapper实例
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder mapperBuilder) {
        // 创建ObjectMapper实例
        ObjectMapper build = mapperBuilder.createXmlMapper(false).build();
        
        // 设置序列化包含策略，排除null值
        build.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // 创建并配置序列化模块
        SimpleModule module = new SimpleModule();
        
        // 为Long类型和long基本类型添加ToString序列化器
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        
        // 注册模块到ObjectMapper
        build.registerModule(module);
        
        return build;
    }
}