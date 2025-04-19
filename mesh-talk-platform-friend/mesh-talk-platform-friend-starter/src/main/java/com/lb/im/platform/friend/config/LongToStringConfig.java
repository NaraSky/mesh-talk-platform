package com.lb.im.platform.friend.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
@Configuration
/**
 * Spring配置类，用于配置Jackson的ObjectMapper，将Long类型序列化为字符串而非数字格式。
 */
public class LongToStringConfig {
    @Bean
    /**
     * 配置并创建自定义的ObjectMapper实例，将Long类型转换为字符串。
     * @param mapperBuilder 用于构建ObjectMapper的Jackson构建器
     * @return 配置后的ObjectMapper实例
     */
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder mapperBuilder) {
        ObjectMapper build = mapperBuilder.createXmlMapper(false).build();

        // 配置ObjectMapper排除null值字段
        build.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 注册序列化器模块，将Long及其基本类型转换为字符串
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        build.registerModule(module);

        return build;
    }
}
