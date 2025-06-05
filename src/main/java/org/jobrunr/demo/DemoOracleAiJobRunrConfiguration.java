package org.jobrunr.demo;

import org.jobrunr.demo.system.BytesToUUIDConverter;
import org.jobrunr.demo.system.DoubleArrayToJdbcValueConverter;
import org.jobrunr.demo.system.UUIDToBytesConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DemoOracleAiJobRunrConfiguration {

    @Bean
    public JdbcCustomConversions jdbcCustomConversions() {
        List<?> converters = List.of(
                new UUIDToBytesConverter(),
                new BytesToUUIDConverter(),
                new DoubleArrayToJdbcValueConverter());
        return new JdbcCustomConversions(converters);
    }
}
