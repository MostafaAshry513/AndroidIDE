package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.interpreter.PythonLexer
import com.example.interpreter.PythonLinter
import com.example.interpreter.PythonRuntime
import com.example.shortcuts.ShortcutAction
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `test app name resource is CodeStudio`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CodeStudio", appName)
    }

    @Test
    fun `test PythonRuntime basic arithmetic and functions`() = runBlocking {
        val outputs = mutableListOf<String>()
        val runtime = PythonRuntime(
            onPrint = { outputs.add(it) },
            onError = { outputs.add("ERR: $it") },
            onInputRequest = { "" }
        )

        val script = """
            def add(a, b):
                return a + b
            
            x = add(15, 27)
            print(f"Result: {x}")
        """.trimIndent()

        val summary = runtime.executeScript(script)
        assertTrue(summary.success)
        assertTrue(outputs.any { it.contains("Result: 42") })
    }

    @Test
    fun `test PythonRuntime loop and list comprehension`() = runBlocking {
        val outputs = mutableListOf<String>()
        val runtime = PythonRuntime(
            onPrint = { outputs.add(it) },
            onError = { outputs.add("ERR: $it") },
            onInputRequest = { "" }
        )

        val script = """
            numbers = [1, 2, 3, 4, 5]
            squares = [x**2 for x in numbers]
            print(f"Squares: {squares}")
        """.trimIndent()

        val summary = runtime.executeScript(script)
        assertTrue(summary.success)
        assertTrue(outputs.any { it.contains("1, 4, 9, 16, 25") })
    }

    @Test
    fun `test PythonLinter detects unclosed quotes`() {
        val badScript = "text = \"unclosed string"
        val errors = PythonLinter.lint(badScript)
        assertFalse(errors.isEmpty())
        assertTrue(errors.any { it.message.contains("Unclosed string") })
    }

    @Test
    fun `test PythonLexer produces annotated string`() {
        val code = "def hello():\n    return 123"
        val annotated = PythonLexer.highlightPython(code)
        assertNotNull(annotated)
        assertEquals(code, annotated.text)
    }

    @Test
    fun `test AiCopilotService offline fallback generates helpful code analysis`() = runBlocking {
        val service = com.example.ai.AiCopilotService()
        val response = service.chatWithCopilot(
            userPrompt = "Explain this code",
            activeCode = "def factorial(n):\n    return 1 if n <= 1 else n * factorial(n - 1)",
            language = "python",
            fileName = "main.py"
        )
        assertNotNull(response)
        assertTrue(response.contains("Code Analysis") || response.contains("factorial"))
    }

    @Test
    fun `test PolyglotRuntime executes Javascript and produces output`() = runBlocking {
        val prints = mutableListOf<String>()
        val runtime = com.example.interpreter.PolyglotRuntime(
            onPrint = { prints.add(it) },
            onError = { prints.add("ERR: $it") },
            onInputRequest = { "" }
        )
        val jsCode = "console.log('Hello from Node!');"
        val summary = runtime.execute(jsCode, "javascript", "test.js")
        assertTrue(summary.success)
        assertTrue(prints.any { it.contains("Hello from Node!") })
    }
}
