package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FormatIndentDecrease
import androidx.compose.material.icons.filled.FormatIndentIncrease
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeAccent

@Composable
fun SmartAutoCompleteBar(
    language: String,
    onInsertText: (String) -> Unit,
    onIndent: () -> Unit,
    onOutdent: () -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onTriggerAiCopilot: () -> Unit,
    modifier: Modifier = Modifier
) {
    val languageKeywords = remember(language) {
        when (language.lowercase()) {
            "python" -> listOf(
                "def " to "def name():",
                "class " to "class Name:",
                "for  in " to "for item in list:",
                "async def " to "async def run():",
                "if __name__" to "if __name__ == '__main__':",
                "try-except" to "try:\n    pass\nexcept Exception as e:\n    print(e)",
                "import " to "import ",
                "return " to "return ",
                "lambda " to "lambda x: ",
                "self." to "self."
            )
            "javascript", "typescript" -> listOf(
                "const " to "const ",
                "function " to "function name() {}",
                "export " to "export ",
                "async () =>" to "async () => {}",
                "useState" to "const [state, setState] = useState()",
                "useEffect" to "useEffect(() => {}, [])",
                "import from" to "import {} from ''",
                "console.log" to "console.log()",
                "return " to "return ",
                "=>" to " => "
            )
            "rust" -> listOf(
                "fn " to "fn name() -> () {}",
                "pub struct " to "pub struct Name {}",
                "impl " to "impl Name {}",
                "let mut " to "let mut ",
                "match " to "match x {\n    _ => (),\n}",
                "println!" to "println!(\"{}\", );",
                "Ok()" to "Ok()",
                "Err()" to "Err()",
                "Result<>" to "Result<T, E>"
            )
            "c", "cpp" -> listOf(
                "#include" to "#include <iostream>",
                "std::vector" to "std::vector<int>",
                "std::cout" to "std::cout <<  << std::endl;",
                "int main()" to "int main() {\n    return 0;\n}",
                "class " to "class Name {\npublic:\n};",
                "auto " to "auto ",
                "template" to "template<typename T>"
            )
            "html" -> listOf(
                "<div>" to "<div></div>",
                "<span>" to "<span></span>",
                "<button>" to "<button></button>",
                "<script>" to "<script>\n</script>",
                "<style>" to "<style>\n</style>",
                "class=\"\"" to "class=\"\""
            )
            "css" -> listOf(
                "flex" to "display: flex;\njustify-content: center;\nalign-items: center;",
                "grid" to "display: grid;\ngrid-template-columns: repeat(3, 1fr);",
                "padding" to "padding: 8px;",
                "margin" to "margin: 0 auto;",
                "color" to "color: #ffffff;",
                "bg" to "background-color: #1e1e1e;"
            )
            "sql" -> listOf(
                "SELECT" to "SELECT * FROM ",
                "WHERE" to "WHERE ",
                "ORDER BY" to "ORDER BY ",
                "INSERT INTO" to "INSERT INTO table_name VALUES ()",
                "CREATE TABLE" to "CREATE TABLE items (id INTEGER PRIMARY KEY, name TEXT)",
                "JOIN" to "INNER JOIN "
            )
            else -> listOf(
                "return " to "return ",
                "function " to "function ",
                "class " to "class "
            )
        }
    }

    val quickSymbols = listOf(
        "{", "}", "(", ")", "[", "]", "\"", "'", "`",
        ":", ";", "=", "->", "=>", "==", "!=", "<", ">",
        "+", "-", "*", "/", "$", "_", "#", "@", ".", ","
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
    ) {
        // Row 1: Language-specific Smart Snippet Chips
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(Color(0xFF252526))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // AI Copilot Quick Assist Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(VsCodeAccent)
                    .clickable(onClick = onTriggerAiCopilot)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Copilot",
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "AI Copilot",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Language Keywords Chips
            for ((label, snippet) in languageKeywords) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF333333))
                        .clickable { onInsertText(snippet) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9CDCFE),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Row 2: Universal Symbols & Cursor Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(Color(0xFF1E1E1E))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            // Cursor Left / Right
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF2A2A2A))
                    .clickable(onClick = onMoveCursorLeft)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Left", tint = Color.LightGray, modifier = Modifier.size(12.dp))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF2A2A2A))
                    .clickable(onClick = onMoveCursorRight)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Right", tint = Color.LightGray, modifier = Modifier.size(12.dp))
            }

            // Indent / Outdent
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF2A2A2A))
                    .clickable(onClick = onIndent)
            ) {
                Icon(Icons.Default.FormatIndentIncrease, contentDescription = "Indent", tint = Color(0xFF4EC9B0), modifier = Modifier.size(12.dp))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF2A2A2A))
                    .clickable(onClick = onOutdent)
            ) {
                Icon(Icons.Default.FormatIndentDecrease, contentDescription = "Outdent", tint = Color(0xFFCE9178), modifier = Modifier.size(12.dp))
            }

            // Tab / Space
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF2A2A2A))
                    .clickable { onInsertText("    ") }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text("TAB", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // Symbols
            for (sym in quickSymbols) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF262626))
                        .clickable { onInsertText(sym) }
                ) {
                    Text(
                        text = sym,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDCDCDC),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
