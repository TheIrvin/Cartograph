package com.tracemap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;

@SpringBootApplication
public class TraceMapApplication {

    @Bean
    @ConfigurationProperties("spring.datasource")
    DataSource dataSource(
            DataSourceProperties properties,
            @Value("${tracemap.sqlite.path}") String sqlitePath) throws IOException {
        Path database = Path.of(sqlitePath).toAbsolutePath();
        Path parent = database.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        return properties.initializeDataSourceBuilder().build();
    }

    public static void main(String[] args) {
        SpringApplication.run(TraceMapApplication.class, args);
    }
}
