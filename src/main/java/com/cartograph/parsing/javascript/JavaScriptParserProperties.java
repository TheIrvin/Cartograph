package com.cartograph.parsing.javascript;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "cartograph.parser.javascript")
public record JavaScriptParserProperties(@DefaultValue("true") boolean enabled) {
    @ConstructorBinding
    public JavaScriptParserProperties { }

    public JavaScriptParserProperties() { this(true); }
}
