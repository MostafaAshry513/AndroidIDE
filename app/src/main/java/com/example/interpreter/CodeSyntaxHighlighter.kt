package com.example.interpreter

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import java.util.regex.Pattern

data class EditorColorScheme(
    val keyword: Color = Color(0xFF569CD6),        // VS Code blue
    val builtIn: Color = Color(0xFF4EC9B0),        // VS Code teal
    val stringColor: Color = Color(0xFFCE9178),    // VS Code orange-brown
    val number: Color = Color(0xFFB5CEA8),         // VS Code light green
    val comment: Color = Color(0xFF6A9955),        // VS Code green
    val decorator: Color = Color(0xFFDCDCAA),      // VS Code yellow-tan
    val functionName: Color = Color(0xFFDCDCAA),   // VS Code yellow
    val className: Color = Color(0xFF4EC9B0),      // VS Code teal
    val variable: Color = Color(0xFF9CDCFE),       // Light blue
    val tagColor: Color = Color(0xFF569CD6),       // Blue
    val attributeColor: Color = Color(0xFF9CDCFE), // Light blue
    val operatorColor: Color = Color(0xFFD4D4D4),  // Grey-white
    val typeColor: Color = Color(0xFF4EC9B0),      // Teal
    val macroColor: Color = Color(0xFFC586C0),     // Purple / Pink
    val defaultText: Color = Color(0xFFD4D4D4)
)

object CodeSyntaxHighlighter {

    private val NUMBER_PATTERN = Pattern.compile("\\b(0[xX][0-9a-fA-F]+|0[bB][01]+|[0-9]+(\\.[0-9]+)?([eE][+-]?[0-9]+)?f?|\\d+L?)\\b")

    // Language Keywords Sets
    private val PYTHON_KEYWORDS = setOf(
        "and", "as", "assert", "async", "await", "break", "class", "continue",
        "def", "del", "elif", "else", "except", "finally", "for", "from",
        "global", "if", "import", "in", "is", "lambda", "nonlocal", "not",
        "or", "pass", "raise", "return", "try", "while", "with", "yield",
        "match", "case"
    )

    private val PYTHON_BUILTINS = setOf(
        "abs", "all", "any", "ascii", "bin", "bool", "breakpoint", "bytearray",
        "bytes", "callable", "chr", "classmethod", "compile", "complex", "delattr",
        "dict", "dir", "divmod", "enumerate", "eval", "exec", "filter", "float",
        "format", "frozenset", "getattr", "globals", "hasattr", "hash", "help",
        "hex", "id", "input", "int", "isinstance", "issubclass", "iter", "len",
        "list", "locals", "map", "max", "memoryview", "min", "next", "object",
        "oct", "open", "ord", "pow", "print", "property", "range", "repr",
        "reversed", "round", "set", "setattr", "slice", "sorted", "staticmethod",
        "str", "sum", "super", "tuple", "type", "vars", "zip", "__import__",
        "True", "False", "None", "__name__", "__main__", "__init__", "__str__"
    )

    private val JS_TS_KEYWORDS = setOf(
        "break", "case", "catch", "class", "const", "continue", "debugger", "default",
        "delete", "do", "else", "export", "extends", "finally", "for", "function",
        "if", "import", "in", "instanceof", "new", "return", "super", "switch",
        "this", "throw", "try", "typeof", "var", "void", "while", "with", "yield",
        "let", "static", "enum", "await", "async", "null", "undefined", "true", "false",
        "type", "interface", "implements", "readonly", "as", "keyof", "declare", "namespace"
    )

    private val JS_TS_BUILTINS = setOf(
        "console", "window", "document", "Math", "JSON", "Promise", "Array", "Object",
        "String", "Number", "Boolean", "Map", "Set", "fetch", "setTimeout", "setInterval",
        "clearTimeout", "clearInterval", "process", "require", "module", "exports", "React",
        "useState", "useEffect", "useMemo", "useCallback", "useRef", "useContext"
    )

    private val KOTLIN_KEYWORDS = setOf(
        "package", "import", "class", "interface", "fun", "val", "var", "if", "else",
        "when", "for", "while", "do", "try", "catch", "finally", "throw", "return",
        "object", "companion", "data", "sealed", "enum", "private", "protected", "public",
        "internal", "override", "suspend", "null", "true", "false", "this", "super",
        "by", "lazy", "lateinit", "inline", "crossinline", "noinline", "tailrec", "operator"
    )

    private val JAVA_KEYWORDS = setOf(
        "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
        "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
        "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
        "interface", "long", "native", "new", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
        "throw", "throws", "transient", "try", "void", "volatile", "while", "null", "true", "false", "record"
    )

    private val CPP_KEYWORDS = setOf(
        "auto", "break", "case", "catch", "class", "const", "constexpr", "continue", "default",
        "delete", "do", "else", "enum", "explicit", "export", "extern", "false", "for", "friend",
        "goto", "if", "inline", "mutable", "namespace", "new", "noexcept", "nullptr", "operator",
        "private", "protected", "public", "register", "reinterpret_cast", "return", "sizeof",
        "static", "static_assert", "static_cast", "struct", "switch", "template", "this",
        "thread_local", "throw", "true", "try", "typedef", "typeid", "typename", "union",
        "using", "virtual", "void", "volatile", "while", "include", "define", "ifndef", "endif"
    )

    private val RUST_KEYWORDS = setOf(
        "as", "break", "const", "continue", "crate", "else", "enum", "extern", "false", "fn",
        "for", "if", "impl", "in", "let", "loop", "match", "mod", "move", "mut", "pub", "ref",
        "return", "self", "Self", "static", "struct", "super", "trait", "true", "type", "unsafe",
        "use", "where", "while", "async", "await", "dyn", "Some", "None", "Ok", "Err"
    )

    private val GO_KEYWORDS = setOf(
        "break", "default", "func", "interface", "select", "case", "defer", "go", "map",
        "struct", "chan", "else", "goto", "package", "switch", "const", "fallthrough",
        "if", "range", "type", "continue", "for", "import", "return", "var", "nil", "true", "false"
    )

    private val SQL_KEYWORDS = setOf(
        "SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE",
        "CREATE", "TABLE", "DROP", "ALTER", "ADD", "JOIN", "INNER", "LEFT", "RIGHT", "FULL",
        "ON", "GROUP", "BY", "ORDER", "ASC", "DESC", "HAVING", "LIMIT", "OFFSET", "AND",
        "OR", "NOT", "NULL", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "INDEX", "UNIQUE",
        "DISTINCT", "AS", "COUNT", "SUM", "AVG", "MIN", "MAX", "CASE", "WHEN", "THEN", "ELSE", "END",
        "select", "from", "where", "insert", "into", "values", "update", "set", "delete",
        "create", "table", "drop", "alter", "join", "on", "group", "by", "order", "limit"
    )

    private val CSS_KEYWORDS = setOf(
        "display", "position", "flex", "grid", "margin", "padding", "width", "height", "color",
        "background", "border", "font-family", "font-size", "align-items", "justify-content",
        "flex-direction", "gap", "overflow", "cursor", "transition", "transform", "box-shadow",
        "border-radius", "z-index", "top", "left", "right", "bottom", "important"
    )

    fun highlight(code: String, language: String, colors: EditorColorScheme = EditorColorScheme()): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            if (code.isEmpty()) return@buildAnnotatedString

            val lang = language.lowercase().trim()
            when {
                lang in listOf("python", "py") -> highlightPython(code, this, colors)
                lang in listOf("javascript", "typescript", "js", "ts", "jsx", "tsx", "mjs", "cjs") -> highlightJavaScript(code, this, colors)
                lang in listOf("html", "htm", "xml", "svg") -> highlightHtml(code, this, colors)
                lang in listOf("css", "scss", "sass", "less") -> highlightCss(code, this, colors)
                lang in listOf("kotlin", "kt", "kts") -> highlightKotlin(code, this, colors)
                lang in listOf("java") -> highlightJava(code, this, colors)
                lang in listOf("c", "cpp", "cc", "cxx", "h", "hpp", "csharp", "cs") -> highlightCpp(code, this, colors)
                lang in listOf("rust", "rs") -> highlightRust(code, this, colors)
                lang in listOf("go", "golang") -> highlightGo(code, this, colors)
                lang in listOf("sql", "mysql", "postgres", "sqlite") -> highlightSql(code, this, colors)
                lang in listOf("json") -> highlightJson(code, this, colors)
                lang in listOf("yaml", "yml", "toml") -> highlightYaml(code, this, colors)
                lang in listOf("markdown", "md") -> highlightMarkdown(code, this, colors)
                lang in listOf("shell", "sh", "bash", "zsh", "dockerfile") -> highlightShell(code, this, colors)
                lang in listOf("php", "ruby", "rb", "dart", "swift") -> highlightGeneralPolyglot(code, this, colors)
                else -> highlightGeneric(code, this, colors)
            }
        }
    }

    private fun highlightPython(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val kwPattern = Pattern.compile("\\b(" + PYTHON_KEYWORDS.joinToString("|") + ")\\b")
        val builtinPattern = Pattern.compile("\\b(" + PYTHON_BUILTINS.joinToString("|") + ")\\b")
        val commentPattern = Pattern.compile("#.*")
        val stringPattern = Pattern.compile("\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"[^\"\\\\\n]*(\\\\.[^\"\\\\\n]*)*\"|'[^'\\\\\n]*(\\\\.[^'\\\\\n]*)*'")
        val decoratorPattern = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_.]*")
        val funcPattern = Pattern.compile("def\\s+([a-zA-Z_][a-zA-Z0-9_]*)")
        val classPattern = Pattern.compile("class\\s+([a-zA-Z_][a-zA-Z0-9_]*)")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        applyPattern(code, kwPattern, builder, SpanStyle(color = colors.keyword, fontWeight = FontWeight.SemiBold))
        applyPattern(code, builtinPattern, builder, SpanStyle(color = colors.builtIn))
        applyPattern(code, decoratorPattern, builder, SpanStyle(color = colors.decorator))

        val funcMatcher = funcPattern.matcher(code)
        while (funcMatcher.find()) {
            if (funcMatcher.groupCount() >= 1) {
                builder.addStyle(SpanStyle(color = colors.functionName), funcMatcher.start(1), funcMatcher.end(1))
            }
        }

        val classMatcher = classPattern.matcher(code)
        while (classMatcher.find()) {
            if (classMatcher.groupCount() >= 1) {
                builder.addStyle(SpanStyle(color = colors.className, fontWeight = FontWeight.Medium), classMatcher.start(1), classMatcher.end(1))
            }
        }

        applyPattern(code, stringPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, commentPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightJavaScript(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val kwPattern = Pattern.compile("\\b(" + JS_TS_KEYWORDS.joinToString("|") + ")\\b")
        val builtinPattern = Pattern.compile("\\b(" + JS_TS_BUILTINS.joinToString("|") + ")\\b")
        val stringPattern = Pattern.compile("`[\\s\\S]*?`|\"[^\"\\\\\n]*(\\\\.[^\"\\\\\n]*)*\"|'[^'\\\\\n]*(\\\\.[^'\\\\\n]*)*'")
        val commentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/")
        val funcPattern = Pattern.compile("(?:function|const|let|var)\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*=?\\s*(?:function|\\()")
        val jsxTagPattern = Pattern.compile("</?[a-zA-Z][a-zA-Z0-9.-]*")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        applyPattern(code, kwPattern, builder, SpanStyle(color = colors.keyword, fontWeight = FontWeight.SemiBold))
        applyPattern(code, builtinPattern, builder, SpanStyle(color = colors.builtIn))
        applyPattern(code, jsxTagPattern, builder, SpanStyle(color = colors.tagColor, fontWeight = FontWeight.Medium))

        val funcMatcher = funcPattern.matcher(code)
        while (funcMatcher.find()) {
            if (funcMatcher.groupCount() >= 1) {
                builder.addStyle(SpanStyle(color = colors.functionName), funcMatcher.start(1), funcMatcher.end(1))
            }
        }

        applyPattern(code, stringPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, commentPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightKotlin(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val kwPattern = Pattern.compile("\\b(" + KOTLIN_KEYWORDS.joinToString("|") + ")\\b")
        val stringPattern = Pattern.compile("\"\"\"[\\s\\S]*?\"\"\"|\"[^\"\\\\\n]*(\\\\.[^\"\\\\\n]*)*\"")
        val commentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/")
        val funcPattern = Pattern.compile("fun\\s+([a-zA-Z_][a-zA-Z0-9_]*)")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        applyPattern(code, kwPattern, builder, SpanStyle(color = colors.keyword, fontWeight = FontWeight.SemiBold))

        val funcMatcher = funcPattern.matcher(code)
        while (funcMatcher.find()) {
            if (funcMatcher.groupCount() >= 1) {
                builder.addStyle(SpanStyle(color = colors.functionName), funcMatcher.start(1), funcMatcher.end(1))
            }
        }

        applyPattern(code, stringPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, commentPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightJava(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val kwPattern = Pattern.compile("\\b(" + JAVA_KEYWORDS.joinToString("|") + ")\\b")
        val stringPattern = Pattern.compile("\"[^\"\\\\\n]*(\\\\.[^\"\\\\\n]*)*\"|'[^'\\\\\n]*(\\\\.[^'\\\\\n]*)*'")
        val commentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/")
        val annoPattern = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        applyPattern(code, kwPattern, builder, SpanStyle(color = colors.keyword, fontWeight = FontWeight.SemiBold))
        applyPattern(code, annoPattern, builder, SpanStyle(color = colors.decorator))
        applyPattern(code, stringPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, commentPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightCpp(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val kwPattern = Pattern.compile("\\b(" + CPP_KEYWORDS.joinToString("|") + ")\\b")
        val preprocessorPattern = Pattern.compile("#\\s*(include|define|ifndef|ifdef|endif|pragma|if|else).*")
        val stringPattern = Pattern.compile("\"[^\"\\\\\n]*(\\\\.[^\"\\\\\n]*)*\"|'[^'\\\\\n]*(\\\\.[^'\\\\\n]*)*'")
        val commentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        applyPattern(code, kwPattern, builder, SpanStyle(color = colors.keyword, fontWeight = FontWeight.SemiBold))
        applyPattern(code, preprocessorPattern, builder, SpanStyle(color = colors.macroColor))
        applyPattern(code, stringPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, commentPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightRust(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val kwPattern = Pattern.compile("\\b(" + RUST_KEYWORDS.joinToString("|") + ")\\b")
        val macroPattern = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*!")
        val stringPattern = Pattern.compile("\"[^\"\\\\\n]*(\\\\.[^\"\\\\\n]*)*\"|r#\"[\\s\\S]*?\"#")
        val commentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        applyPattern(code, kwPattern, builder, SpanStyle(color = colors.keyword, fontWeight = FontWeight.SemiBold))
        applyPattern(code, macroPattern, builder, SpanStyle(color = colors.macroColor, fontWeight = FontWeight.Bold))
        applyPattern(code, stringPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, commentPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightGo(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val kwPattern = Pattern.compile("\\b(" + GO_KEYWORDS.joinToString("|") + ")\\b")
        val stringPattern = Pattern.compile("`[\\s\\S]*?`|\"[^\"\\\\\n]*(\\\\.[^\"\\\\\n]*)*\"")
        val commentPattern = Pattern.compile("//.*|/\\*[\\s\\S]*?\\*/")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        applyPattern(code, kwPattern, builder, SpanStyle(color = colors.keyword, fontWeight = FontWeight.SemiBold))
        applyPattern(code, stringPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, commentPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightSql(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val kwPattern = Pattern.compile("(?i)\\b(" + SQL_KEYWORDS.joinToString("|") + ")\\b")
        val stringPattern = Pattern.compile("'[^']*'|\"[^\"]*\"")
        val commentPattern = Pattern.compile("--.*|/\\*[\\s\\S]*?\\*/")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        applyPattern(code, kwPattern, builder, SpanStyle(color = colors.keyword, fontWeight = FontWeight.Bold))
        applyPattern(code, stringPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, commentPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightHtml(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val tagPattern = Pattern.compile("</?[a-zA-Z0-9_-]+")
        val attrPattern = Pattern.compile("\\s+([a-zA-Z0-9_-]+)=")
        val strPattern = Pattern.compile("\"[^\"]*\"|'[^']*'")
        val commentPattern = Pattern.compile("<!--[\\s\\S]*?-->")

        applyPattern(code, tagPattern, builder, SpanStyle(color = colors.tagColor, fontWeight = FontWeight.SemiBold))
        val attrMatcher = attrPattern.matcher(code)
        while (attrMatcher.find()) {
            if (attrMatcher.groupCount() >= 1) {
                builder.addStyle(SpanStyle(color = colors.attributeColor), attrMatcher.start(1), attrMatcher.end(1))
            }
        }
        applyPattern(code, strPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, commentPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightCss(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val selectorPattern = Pattern.compile("^[^{]+(?=\\{)", Pattern.MULTILINE)
        val propertyPattern = Pattern.compile("([a-zA-Z0-9_-]+)\\s*:")
        val stringPattern = Pattern.compile("\"[^\"]*\"|'[^']*'")
        val commentPattern = Pattern.compile("/\\*[\\s\\S]*?\\*/")

        applyPattern(code, selectorPattern, builder, SpanStyle(color = colors.decorator, fontWeight = FontWeight.Medium))
        val propMatcher = propertyPattern.matcher(code)
        while (propMatcher.find()) {
            if (propMatcher.groupCount() >= 1) {
                builder.addStyle(SpanStyle(color = colors.builtIn), propMatcher.start(1), propMatcher.end(1))
            }
        }
        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        applyPattern(code, stringPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, commentPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightJson(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val keyPattern = Pattern.compile("\"([^\"]+)\"\\s*:")
        val strPattern = Pattern.compile(":\\s*\"([^\"]*)\"")
        val kwPattern = Pattern.compile("\\b(true|false|null)\\b")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        val keyMatcher = keyPattern.matcher(code)
        while (keyMatcher.find()) {
            if (keyMatcher.groupCount() >= 1) {
                builder.addStyle(SpanStyle(color = colors.builtIn), keyMatcher.start(1), keyMatcher.end(1))
            }
        }
        val strMatcher = strPattern.matcher(code)
        while (strMatcher.find()) {
            if (strMatcher.groupCount() >= 1) {
                builder.addStyle(SpanStyle(color = colors.stringColor), strMatcher.start(1), strMatcher.end(1))
            }
        }
        applyPattern(code, kwPattern, builder, SpanStyle(color = colors.keyword))
    }

    private fun highlightYaml(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val keyPattern = Pattern.compile("^\\s*([a-zA-Z0-9_.-]+):", Pattern.MULTILINE)
        val stringPattern = Pattern.compile("\"[^\"]*\"|'[^']*'")
        val commentPattern = Pattern.compile("#.*")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        val keyMatcher = keyPattern.matcher(code)
        while (keyMatcher.find()) {
            if (keyMatcher.groupCount() >= 1) {
                builder.addStyle(SpanStyle(color = colors.builtIn, fontWeight = FontWeight.SemiBold), keyMatcher.start(1), keyMatcher.end(1))
            }
        }
        applyPattern(code, stringPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, commentPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightMarkdown(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val headerPattern = Pattern.compile("^(#{1,6}\\s+.*)$", Pattern.MULTILINE)
        val codeBlockPattern = Pattern.compile("```[\\s\\S]*?```|`[^`\n]+`")
        val linkPattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)")

        applyPattern(code, headerPattern, builder, SpanStyle(color = colors.keyword, fontWeight = FontWeight.Bold))
        applyPattern(code, codeBlockPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, linkPattern, builder, SpanStyle(color = colors.builtIn))
    }

    private fun highlightShell(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val kwPattern = Pattern.compile("\\b(if|then|else|elif|fi|case|esac|for|while|until|do|done|in|function|return|exit|export|source|alias)\\b")
        val comPattern = Pattern.compile("#.*")
        val strPattern = Pattern.compile("\"[^\"]*\"|'[^']*'")
        val varPattern = Pattern.compile("\\$[a-zA-Z0-9_]+|\\$\\{[^}]+\\}")

        applyPattern(code, kwPattern, builder, SpanStyle(color = colors.keyword, fontWeight = FontWeight.SemiBold))
        applyPattern(code, varPattern, builder, SpanStyle(color = colors.variable))
        applyPattern(code, strPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, comPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightGeneralPolyglot(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val kwPattern = Pattern.compile("\\b(def|class|function|var|let|const|val|fun|import|package|public|private|if|else|for|while|return|new|end|begin)\\b")
        val strPattern = Pattern.compile("\"[^\"]*\"|'[^']*'")
        val comPattern = Pattern.compile("//.*|#.*|/\\*[\\s\\S]*?\\*/")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        applyPattern(code, kwPattern, builder, SpanStyle(color = colors.keyword, fontWeight = FontWeight.SemiBold))
        applyPattern(code, strPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, comPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun highlightGeneric(code: String, builder: AnnotatedString.Builder, colors: EditorColorScheme) {
        val strPattern = Pattern.compile("\"[^\"]*\"|'[^']*'")
        val comPattern = Pattern.compile("//.*|#.*|/\\*[\\s\\S]*?\\*/")

        applyPattern(code, NUMBER_PATTERN, builder, SpanStyle(color = colors.number))
        applyPattern(code, strPattern, builder, SpanStyle(color = colors.stringColor))
        applyPattern(code, comPattern, builder, SpanStyle(color = colors.comment))
    }

    private fun applyPattern(code: String, pattern: Pattern, builder: AnnotatedString.Builder, style: SpanStyle) {
        val matcher = pattern.matcher(code)
        while (matcher.find()) {
            builder.addStyle(style, matcher.start(), matcher.end())
        }
    }
}
