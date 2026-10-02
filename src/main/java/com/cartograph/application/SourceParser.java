package com.cartograph.application;

import com.cartograph.graph.model.ParsedFile;
import com.cartograph.graph.model.SourceFile;

@FunctionalInterface
public interface SourceParser {
    ParsedFile parse(SourceFile file);
}
