package com.studyroom.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.time.Instant;

@Configuration
@EnableScheduling
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        // 配置JsonSerializer以输出毫秒整数而非秒小数
        SimpleModule module = new SimpleModule();
        module.addSerializer(Instant.class, new InstantSerializer());
        mapper.registerModule(module);

        return mapper;
    }

    static class InstantSerializer extends JsonSerializer<Instant> {
        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeNumber(value.toEpochMilli());
        }
    }
}