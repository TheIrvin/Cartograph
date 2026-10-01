package com.tracemap.parsing.javascript;

import com.tracemap.application.SourceParser;
import com.tracemap.graph.StableNodeId;
import com.tracemap.graph.model.*;
import java.util.*;
import org.treesitter.*;

/** Tree-sitter-backed adapter. It deliberately emits relationships only when the AST proves them. */
public final class JavaScriptTypeScriptParser implements SourceParser {
    private static final Set<String> EXTENSIONS = Set.of("js", "jsx", "ts", "tsx");
    private static final Set<String> DEFINITIONS = Set.of("function_declaration", "class_declaration", "method_definition");

    @Override
    public ParsedFile parse(SourceFile file) {
        String path = StableNodeId.normalizePath(file.path());
        String extension = extension(path);
        if (!EXTENSIONS.contains(extension)) {
            return empty(path, new GraphWarning("UNSUPPORTED_FILE", "Unsupported source extension", path, null));
        }
        TSLanguage language = switch (extension) {
            case "ts" -> new TreeSitterTypescript();
            case "tsx" -> new TreeSitterTsx();
            default -> new TreeSitterJavascript();
        };
        try (TSParser parser = new TSParser(); TSLanguage ignored = language) {
            if (!parser.setLanguage(language)) throw new IllegalStateException("Incompatible tree-sitter grammar");
            try (TSTree tree = parser.parseString(null, file.content())) {
                TSNode root = tree.getRootNode();
                List<GraphWarning> warnings = new ArrayList<>();
                if (root.hasError()) warnings.add(new GraphWarning("SYNTAX_ERROR", "Tree-sitter recovered from invalid syntax", path, line(root)));
                List<GraphNode> nodes = new ArrayList<>();
                List<GraphEdge> edges = new ArrayList<>();
                List<String> exports = new ArrayList<>();
                GraphNode fileNode = node(path, SymbolKind.FILE, path, root, file.content());
                nodes.add(fileNode);
                Map<String, List<Definition>> definitions = new LinkedHashMap<>();
                walk(root, n -> {
                    String type = n.getType();
                    if (DEFINITIONS.contains(type)) {
                        Definition definition = definition(n, file.content(), path);
                        if (definition != null) {
                            definitions.computeIfAbsent(definition.name, ignoredName -> new ArrayList<>()).add(definition);
                            nodes.add(definition.node);
                            edges.add(new GraphEdge(fileNode.stableId(), definition.node.stableId(), EdgeKind.CONTAINS, 1.0));
                        }
                    } else if (type.equals("import_statement")) {
                        String module = importedModule(n, file.content());
                        if (module != null) {
                            GraphNode moduleNode = node(path, SymbolKind.MODULE, module, n, file.content());
                            nodes.add(moduleNode);
                            edges.add(new GraphEdge(moduleNode.stableId(), fileNode.stableId(), EdgeKind.IMPORTS, 1.0));
                        }
                    } else if (type.equals("export_statement")) exports.addAll(exportNames(n, file.content()));
                    else if (type.equals("call_expression")) {
                        TSNode target = n.getChildByFieldName("function");
                        if (target.isNull() || !target.getType().equals("identifier")) {
                            warnings.add(new GraphWarning("UNRESOLVED_CALL", "Call target cannot be proven: " + text(target, file.content()), path, line(n)));
                            return;
                        }
                        String name = text(target, file.content());
                        Definition caller = enclosingDefinition(n, definitions.values().stream().flatMap(Collection::stream).toList());
                        Definition resolved = resolve(name, caller, definitions.getOrDefault(name, List.of()), file.content());
                        if (resolved == null) warnings.add(new GraphWarning("UNRESOLVED_CALL", "Call target cannot be proven: " + name, path, line(n)));
                        else edges.add(new GraphEdge(caller == null ? fileNode.stableId() : caller.node.stableId(), resolved.node.stableId(), EdgeKind.CALLS, 1.0));
                    } else if (type.equals("arrow_function")) warnings.add(new GraphWarning("UNSUPPORTED_SYNTAX", "Arrow function definitions are not graph nodes", path, line(n)));
                });
                exports.removeIf(e -> e.isBlank());
                nodes.sort(Comparator.comparing(GraphNode::startLine).thenComparing(n -> n.kind() == SymbolKind.FILE ? 0 : 1).thenComparing(GraphNode::name));
                List<GraphEdge> distinctEdges = edges.stream().distinct().toList();
                edges.clear(); edges.addAll(distinctEdges);
                List<GraphWarning> sortedWarnings = warnings.stream().distinct().sorted(Comparator.comparing(w -> Optional.ofNullable(w.line()).orElse(0))).toList();
                warnings.clear(); warnings.addAll(sortedWarnings);
                return new ParsedFile(path, nodes, edges, warnings, exports);
            }
        }
    }

    private static Definition resolve(String name, Definition caller, List<Definition> candidates, String source) {
        if (source.matches("(?s).*\\{\\s*function\\s+" + java.util.regex.Pattern.quote(name) + "\\b.*")
                || source.matches("(?s).*\\beval\\s*\\(.*")
                || source.matches("(?s).*\\{" + java.util.regex.Pattern.quote(name) + "\\}.*")) return null;
        return candidates.stream().filter(d -> d.node.kind() != SymbolKind.METHOD || caller != null)
                .filter(d -> caller != null || !nestedInDefinition(d.ast))
                .filter(d -> caller == null || !shadowed(name, caller.ast, source))
                .min(Comparator.comparingInt(d -> d.node.startLine())).orElse(null);
    }
    private static boolean nestedInDefinition(TSNode ast) { TSNode p = ast.getParent(); while (!p.isNull()) { if (p.getType().equals("function_declaration") || p.getType().equals("method_definition")) return true; p = p.getParent(); } return false; }
    private static boolean shadowed(String name, TSNode caller, String source) {
        String body = text(caller, source);
        return source.matches("(?s).*\\b" + java.util.regex.Pattern.quote(name) + "\\s*=.*")
                || body.matches("(?s).*\\b(?:const|let|var)\\s+" + java.util.regex.Pattern.quote(name) + "\\b.*")
                || body.matches("(?s).*\\b" + java.util.regex.Pattern.quote(name) + "\\s*(?:=|:)" + ".*")
                || body.substring(0, Math.min(body.length(), body.indexOf('{') < 0 ? body.length() : body.indexOf('{'))).matches("(?s).*\\b" + java.util.regex.Pattern.quote(name) + "\\b.*");
    }
    private static Definition enclosingDefinition(TSNode call, List<Definition> defs) {
        return defs.stream().filter(d -> contains(d.ast, call)).max(Comparator.comparingInt(d -> d.node.startLine())).orElse(null);
    }
    private static boolean contains(TSNode parent, TSNode child) { return parent.getStartByte() <= child.getStartByte() && parent.getEndByte() >= child.getEndByte(); }
    private static Definition definition(TSNode ast, String source, String path) {
        String type = ast.getType();
        TSNode nameNode = ast.getChildByFieldName("name");
        if (nameNode.isNull() && type.equals("method_definition")) nameNode = ast.getChildByFieldName("property");
        if (nameNode.isNull()) nameNode = ast.getChildByFieldName("property");
        if (nameNode.isNull()) return null;
        String name = text(nameNode, source);
        SymbolKind kind = type.equals("class_declaration") ? SymbolKind.CLASS : type.equals("method_definition") ? SymbolKind.METHOD : SymbolKind.FUNCTION;
        Definition owner = null;
        TSNode parent = ast.getParent();
        if (kind == SymbolKind.METHOD && !parent.isNull()) {
            TSNode classNode = parent;
            while (!classNode.isNull() && !classNode.getType().equals("class_declaration")) classNode = classNode.getParent();
            if (!classNode.isNull()) {
                TSNode className = classNode.getChildByFieldName("name");
                if (!className.isNull()) name = text(className, source) + "." + name;
            }
        }
        return new Definition(name, node(path, kind, name, ast, source), ast, owner);
    }
    private static GraphNode node(String path, SymbolKind kind, String name, TSNode ast, String source) {
        int line = line(ast), column = ast.getStartPoint().getColumn();
        int end = kind == SymbolKind.FILE ? ast.getEndPoint().getRow() : ast.getEndPoint().getRow() + 1;
        return new GraphNode(StableNodeId.create("", "", path, kind, name, line, column), kind, name, path, line, Math.max(line, end));
    }
    private static String importedModule(TSNode n, String source) {
        for (int i = 0; i < n.getNamedChildCount(); i++) { TSNode c = n.getNamedChild(i); if (c.getType().equals("string")) return unquote(text(c, source)); }
        return null;
    }
    private static List<String> exportNames(TSNode n, String source) {
        List<String> result = new ArrayList<>();
        walk(n, c -> { if (c.getType().equals("export_specifier")) { TSNode alias = c.getChildByFieldName("alias"); result.add(alias.isNull() ? text(c.getChildByFieldName("name"), source) : text(alias, source)); }
            else if ((c.getType().equals("function_declaration") || c.getType().equals("class_declaration")) && c.getParent().getType().equals("export_statement")) result.add(text(c.getChildByFieldName("name"), source)); });
        return result;
    }
    private static String unquote(String value) { return value.length() > 1 ? value.substring(1, value.length() - 1) : value; }
    private static int line(TSNode n) { return n.getStartPoint().getRow() + 1; }
    private static String text(TSNode n, String source) { return n.isNull() ? "" : new String(source.getBytes(java.nio.charset.StandardCharsets.UTF_8), n.getStartByte(), n.getEndByte() - n.getStartByte(), java.nio.charset.StandardCharsets.UTF_8); }
    private static String extension(String path) { int dot = path.lastIndexOf('.'); return dot < 0 ? "" : path.substring(dot + 1).toLowerCase(Locale.ROOT); }
    private static ParsedFile empty(String path, GraphWarning warning) { return new ParsedFile(path, List.of(), List.of(), List.of(warning), List.of()); }
    private static void walk(TSNode node, java.util.function.Consumer<TSNode> consumer) { consumer.accept(node); for (int i = 0; i < node.getNamedChildCount(); i++) walk(node.getNamedChild(i), consumer); }
    private record Definition(String name, GraphNode node, TSNode ast, Definition owner) { }
}
