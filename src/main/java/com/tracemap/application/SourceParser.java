package com.tracemap.application;

import com.tracemap.graph.model.ParsedFile;
import com.tracemap.graph.model.SourceFile;

@FunctionalInterface
public interface SourceParser {
    ParsedFile parse(SourceFile file);
}
