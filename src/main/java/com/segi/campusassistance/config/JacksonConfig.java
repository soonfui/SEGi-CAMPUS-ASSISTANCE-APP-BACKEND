package com.segi.campusassistance.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        
        // 注册 JavaTimeModule 以支持 Java 8 时间类型
        objectMapper.registerModule(new JavaTimeModule());
        
        // 创建自定义模块
        SimpleModule module = new SimpleModule();
        
        // 添加自定义 LocalDate 反序列化器
        module.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) 
                    throws IOException {
                String dateString = p.getText().trim();
                
                // 尝试多种日期格式
                String[] formats = {
                    "MM/dd/yyyy",      // Flutter 格式: 12/09/2025
                    "dd/MM/yyyy",      // 欧洲格式: 09/12/2025
                    "yyyy-MM-dd",      // ISO 格式: 2025-12-09
                    "MM-dd-yyyy",      // 美国格式: 12-09-2025
                    "dd-MM-yyyy"       // 欧洲格式: 09-12-2025
                };
                
                for (String format : formats) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                        return LocalDate.parse(dateString, formatter);
                    } catch (DateTimeParseException e) {
                        // 继续尝试下一个格式
                    }
                }
                
                // 如果所有格式都失败，抛出异常
                throw new InvalidFormatException(
                    p,
                    "Unable to parse date: " + dateString + ". Supported formats: MM/dd/yyyy, dd/MM/yyyy, yyyy-MM-dd, MM-dd-yyyy, dd-MM-yyyy",
                    dateString,
                    LocalDate.class
                );
            }
        });
        
        objectMapper.registerModule(module);
        
        // 配置忽略未知属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        return objectMapper;
    }
}

