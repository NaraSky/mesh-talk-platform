package com.lb.im.platform.common.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.util.Date;

/**
 * 自定义Jackson序列化器，将Date对象序列化为对应的long类型时间戳
 */
public class DateToLongSerializer extends JsonSerializer<Date> {

    /**
     * 将Date对象序列化为JSON数值类型的时间戳
     */
    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        // 将日期对象的getTime()结果写入JSON生成器作为数值类型
        jsonGenerator.writeNumber(date.getTime());
    }

    /**
     * 带类型信息的序列化方法，用于处理多态类型序列化场景
     */
    @Override
    public void serializeWithType(Date value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        // 写入类型前缀标识
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.VALUE_STRING));
        // 执行基础序列化逻辑
        serialize(value, gen, serializers);
        // 写入类型后缀标识
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }
}
