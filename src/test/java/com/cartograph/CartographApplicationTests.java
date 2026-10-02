package com.cartograph;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import com.cartograph.ingestion.github.GitHubProperties;
import com.cartograph.parsing.javascript.JavaScriptParserProperties;
import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CartographApplication.class, properties = {"spring.flyway.enabled=false", "cartograph.github.max-files=37",
        "cartograph.parser.javascript.enabled=false", "GITHUB_TOKEN=offline-test-token"})
class CartographApplicationTests {

    @Autowired Environment environment;
    @Autowired GitHubProperties github;
    @Autowired JavaScriptParserProperties parser;
    @Autowired DataSource dataSource;

    @Test
    void contextLoads() {
        assertThat(CartographApplication.class.getPackageName()).isEqualTo("com.cartograph");
    }

    @Test
    void usesCartographApplicationIdentityAndDatabaseDefault() throws Exception {
        assertThat(environment.getProperty("spring.application.name")).isEqualTo("cartograph");
        assertThat(environment.getProperty("cartograph.sqlite.path")).isEqualTo("./data/cartograph.db");
        try (var connection = dataSource.getConnection()) {
            assertThat(connection.getMetaData().getURL()).contains("data/cartograph.db");
        }
    }

    @Test
    void bindsGitHubSettingsUnderCartographNamespace() {
        assertThat(github.limits().maxFileCount()).isEqualTo(37);
    }

    @Test
    void bindsParserSettingsUnderCartographNamespace() {
        assertThat(parser.enabled()).isFalse();
    }

    @Test
    void preservesGitHubTokenEnvironmentName() {
        assertThat(github.token()).isEqualTo("offline-test-token");
    }

    @Test
    void parserDefaultRemainsEnabledWhenNoOverrideIsPresent() {
        new org.springframework.boot.test.context.runner.ApplicationContextRunner()
                .withUserConfiguration(ParserBindingConfiguration.class)
                .run(context -> assertThat(context.getBean(JavaScriptParserProperties.class).enabled()).isTrue());
    }

    @org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
    @org.springframework.boot.context.properties.EnableConfigurationProperties(JavaScriptParserProperties.class)
    static class ParserBindingConfiguration { }
}
