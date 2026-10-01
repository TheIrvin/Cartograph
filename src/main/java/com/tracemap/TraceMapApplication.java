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
import com.tracemap.application.GraphSnapshotRepository;
import com.tracemap.application.RepositoryFetcher;
import com.tracemap.application.SourceParser;
import com.tracemap.graph.GraphBuilder;
import com.tracemap.ingestion.GitHubUrlNormalizer;
import com.tracemap.ingestion.github.GitHubClient;
import com.tracemap.ingestion.github.GitHubProperties;
import com.tracemap.ingestion.github.GitHubRepositoryFetcher;
import com.tracemap.parsing.javascript.JavaScriptTypeScriptParser;
import com.tracemap.persistence.sqlite.SQLiteGraphSnapshotRepository;

@SpringBootApplication
@org.springframework.boot.context.properties.ConfigurationPropertiesScan
public class TraceMapApplication {

    @Bean GitHubUrlNormalizer gitHubUrlNormalizer() { return new GitHubUrlNormalizer(); }
    @Bean GitHubClient gitHubClient(org.springframework.web.client.RestClient.Builder builder,
            com.fasterxml.jackson.databind.ObjectMapper mapper, GitHubProperties properties) {
        return new GitHubClient(builder, mapper, properties);
    }
    @Bean RepositoryFetcher repositoryFetcher(GitHubClient client, GitHubProperties properties) {
        return new GitHubRepositoryFetcher(client, properties);
    }
    @Bean SourceParser sourceParser() { return new JavaScriptTypeScriptParser(); }
    @Bean GraphBuilder graphBuilder(SourceParser parser) { return new GraphBuilder(parser); }
    @Bean GraphSnapshotRepository graphSnapshotRepository(DataSource dataSource) {
        return new SQLiteGraphSnapshotRepository(dataSource);
    }

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
