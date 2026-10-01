package com.tracemap.parsing.javascript;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tracemap.parser.javascript")
public record JavaScriptParserProperties(boolean enabled) {
    public JavaScriptParserProperties() { this(true); }
}
