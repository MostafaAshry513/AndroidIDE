package com.example.interpreter

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.*
import kotlin.random.Random

data class ExecutionSummary(
    val success: Boolean,
    val durationMs: Long,
    val exitCode: Int,
    val linesExecuted: Int = 0,
    val error: String? = null,
    val language: String = "python",
    val memoryUsageKb: Long = 0
)

data class PythonFunction(
    val name: String,
    val params: List<String>,
    val bodyLines: List<String>
)

class PythonRuntime(
    private val onPrint: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onInputRequest: suspend (prompt: String) -> String
) {

    // Persistent global scope for REPL and script execution
    private val globalScope = mutableMapOf<String, Any?>()

    init {
        resetScope()
    }

    fun resetScope() {
        globalScope.clear()
        globalScope["__name__"] = "__main__"
        globalScope["math.pi"] = Math.PI
        globalScope["math.e"] = Math.E
        globalScope["sys.version"] = "3.12.0 (PyCode Mobile Engine)"
        globalScope["sys.platform"] = "android-arm64"
    }

    suspend fun executeScript(code: String): ExecutionSummary = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        var lineCount = 0

        try {
            val lines = code.lines()
            lineCount = lines.size
            executeBlockLines(lines, globalScope)

            val durationMs = System.currentTimeMillis() - startTime
            ExecutionSummary(
                success = true,
                durationMs = durationMs,
                exitCode = 0,
                linesExecuted = lineCount
            )
        } catch (e: CancellationException) {
            onPrint("\n🛑 [Execution cancelled by user]")
            ExecutionSummary(success = false, durationMs = System.currentTimeMillis() - startTime, exitCode = 130, error = "Cancelled")
        } catch (e: Exception) {
            val errorMsg = e.message ?: e.toString()
            onError("\nTraceback (most recent call last):\n  File \"<main.py>\", line ?, in <module>\n$errorMsg")
            ExecutionSummary(
                success = false,
                durationMs = System.currentTimeMillis() - startTime,
                exitCode = 1,
                error = errorMsg
            )
        }
    }

    suspend fun executeReplLine(line: String): String? = withContext(Dispatchers.Default) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return@withContext null
        if (trimmed.startsWith("#")) return@withContext null

        try {
            if (trimmed.startsWith("print(") && trimmed.endsWith(")")) {
                val content = trimmed.substring(6, trimmed.length - 1)
                val output = evaluatePrintArguments(content, globalScope)
                return@withContext output
            }

            if (trimmed.contains("=") && !trimmed.contains("==") && !trimmed.contains("<=") && !trimmed.contains(">=") && !trimmed.contains("!=")) {
                executeSingleStatement(trimmed, globalScope)
                null
            } else {
                val evalResult = evaluateExpression(trimmed, globalScope)
                if (evalResult != null && evalResult !is Unit) {
                    formatPythonValue(evalResult)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            "Traceback (REPL): ${e.message}"
        }
    }

    private suspend fun executeBlockLines(lines: List<String>, scope: MutableMap<String, Any?>) {
        var i = 0
        while (i < lines.size) {
            val rawLine = lines[i]
            val trimmed = rawLine.trim()

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                i++
                continue
            }

            val currentIndent = getIndentLevel(rawLine)

            // Multi-line block structures
            if (trimmed.startsWith("def ") || trimmed.startsWith("class ") ||
                trimmed.startsWith("if ") || trimmed.startsWith("for ") ||
                trimmed.startsWith("while ") || trimmed.startsWith("try:") ||
                trimmed.startsWith("with ")
            ) {
                // If this is an IF / ELIF / ELSE chain
                if (trimmed.startsWith("if ")) {
                    val ifChain = mutableListOf<Pair<String, List<String>>>() // (condition, body)
                    var elseBody: List<String>? = null

                    // Parse the 'if' part
                    val ifCondition = trimmed.removePrefix("if ").removeSuffix(":").trim()
                    i++
                    val ifBody = mutableListOf<String>()
                    while (i < lines.size) {
                        val nextRaw = lines[i]
                        val nextTrim = nextRaw.trim()
                        if (nextTrim.isEmpty()) {
                            ifBody.add(nextRaw)
                            i++
                            continue
                        }
                        val nextIndent = getIndentLevel(nextRaw)
                        if (nextIndent > currentIndent) {
                            ifBody.add(nextRaw)
                            i++
                        } else {
                            break
                        }
                    }
                    ifChain.add(ifCondition to ifBody)

                    // Check for elif / else siblings
                    while (i < lines.size) {
                        val nextRaw = lines[i]
                        val nextTrim = nextRaw.trim()
                        val nextIndent = getIndentLevel(nextRaw)
                        if (nextIndent == currentIndent && nextTrim.startsWith("elif ")) {
                            val elifCond = nextTrim.removePrefix("elif ").removeSuffix(":").trim()
                            i++
                            val elifBody = mutableListOf<String>()
                            while (i < lines.size) {
                                val nr = lines[i]
                                val nt = nr.trim()
                                if (nt.isEmpty()) {
                                    elifBody.add(nr)
                                    i++
                                    continue
                                }
                                if (getIndentLevel(nr) > currentIndent) {
                                    elifBody.add(nr)
                                    i++
                                } else break
                            }
                            ifChain.add(elifCond to elifBody)
                        } else if (nextIndent == currentIndent && (nextTrim.startsWith("else:") || nextTrim == "else")) {
                            i++
                            val eb = mutableListOf<String>()
                            while (i < lines.size) {
                                val nr = lines[i]
                                val nt = nr.trim()
                                if (nt.isEmpty()) {
                                    eb.add(nr)
                                    i++
                                    continue
                                }
                                if (getIndentLevel(nr) > currentIndent) {
                                    eb.add(nr)
                                    i++
                                } else break
                            }
                            elseBody = eb
                            break
                        } else {
                            break
                        }
                    }

                    // Execute matching branch in if/elif/else
                    var branchExecuted = false
                    for ((cond, body) in ifChain) {
                        if (evaluateCondition(cond, scope)) {
                            executeBlockLines(body, scope)
                            branchExecuted = true
                            break
                        }
                    }
                    if (!branchExecuted && elseBody != null) {
                        executeBlockLines(elseBody, scope)
                    }
                    continue
                }

                // def, for, while, class
                val blockHeader = trimmed
                i++
                val blockBody = mutableListOf<String>()
                while (i < lines.size) {
                    val nextRaw = lines[i]
                    val nextTrim = nextRaw.trim()
                    if (nextTrim.isEmpty()) {
                        blockBody.add(nextRaw)
                        i++
                        continue
                    }
                    val nextIndent = getIndentLevel(nextRaw)
                    if (nextIndent > currentIndent) {
                        blockBody.add(nextRaw)
                        i++
                    } else {
                        break
                    }
                }

                when {
                    blockHeader.startsWith("def ") -> {
                        val defSignature = blockHeader.removePrefix("def ").removeSuffix(":").trim()
                        val funcName = defSignature.substringBefore("(").trim()
                        val paramsStr = defSignature.substringAfter("(").substringBeforeLast(")").trim()
                        val params = if (paramsStr.isEmpty()) emptyList() else paramsStr.split(",").map {
                            it.trim().substringBefore("=").substringBefore(":").trim()
                        }

                        scope[funcName] = PythonFunction(
                            name = funcName,
                            params = params,
                            bodyLines = blockBody
                        )
                    }
                    blockHeader.startsWith("for ") -> {
                        val forHeader = blockHeader.removePrefix("for ").removeSuffix(":").trim()
                        val varName = forHeader.substringBefore(" in ").trim()
                        val iterExpr = forHeader.substringAfter(" in ").trim()
                        val iterable = evaluateExpression(iterExpr, scope)

                        val items: List<Any?> = when (iterable) {
                            is List<*> -> iterable
                            is String -> iterable.map { it.toString() }
                            is Map<*, *> -> iterable.keys.toList()
                            is IntRange -> iterable.toList()
                            is Iterable<*> -> iterable.toList()
                            else -> emptyList()
                        }

                        for (item in items) {
                            if (varName.contains(",")) {
                                val parts = varName.split(",").map { it.trim() }
                                if (item is List<*> && item.size >= parts.size) {
                                    for ((idx, p) in parts.withIndex()) {
                                        scope[p] = item[idx]
                                    }
                                } else if (item is Pair<*, *>) {
                                    if (parts.isNotEmpty()) scope[parts[0]] = item.first
                                    if (parts.size > 1) scope[parts[1]] = item.second
                                }
                            } else {
                                scope[varName] = item
                            }
                            executeBlockLines(blockBody, scope)
                        }
                    }
                    blockHeader.startsWith("while ") -> {
                        val conditionExpr = blockHeader.removePrefix("while ").removeSuffix(":").trim()
                        var iterationCount = 0
                        while (evaluateCondition(conditionExpr, scope) && iterationCount < 20000) {
                            executeBlockLines(blockBody, scope)
                            iterationCount++
                        }
                    }
                    else -> {
                        executeBlockLines(blockBody, scope)
                    }
                }
            } else {
                executeSingleStatement(trimmed, scope)
                i++
            }
        }
    }

    private fun getIndentLevel(line: String): Int {
        var count = 0
        for (c in line) {
            if (c == ' ') count++
            else if (c == '\t') count += 4
            else break
        }
        return count
    }

    private suspend fun executeSingleStatement(stmt: String, scope: MutableMap<String, Any?>) {
        val trimmed = stmt.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed == "pass") return

        // 1. print(...) statement
        if (trimmed.startsWith("print(") && trimmed.endsWith(")")) {
            val content = trimmed.substring(6, trimmed.length - 1)
            val output = evaluatePrintArguments(content, scope)
            onPrint(output)
            return
        }

        // 2. input(...) assigned to variable: var = input("prompt")
        if (trimmed.contains("=") && trimmed.contains("input(")) {
            val varName = trimmed.substringBefore("=").trim()
            val promptStr = if (trimmed.contains("input(\"") || trimmed.contains("input('")) {
                val inner = trimmed.substringAfter("input(").substringBeforeLast(")")
                evaluateExpression(inner, scope)?.toString() ?: ""
            } else ""

            if (promptStr.isNotEmpty()) {
                onPrint(promptStr)
            }
            val userInput = onInputRequest(promptStr)
            scope[varName] = userInput
            return
        }

        // 3. Augmented assignments (+=, -=, *=, /=, %=, **=)
        val augOps = listOf("+=", "-=", "*=", "/=", "%=", "**=")
        for (op in augOps) {
            if (trimmed.contains(op)) {
                val left = trimmed.substringBefore(op).trim()
                val right = trimmed.substringAfter(op).trim()
                val currentVal = evaluateExpression(left, scope)
                val delta = evaluateExpression(right, scope)

                if (currentVal is MutableList<*> && op == "+=") {
                    if (delta is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        (currentVal as MutableList<Any?>).addAll(delta)
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        (currentVal as MutableList<Any?>).add(delta)
                    }
                    return
                }

                if (currentVal is String && op == "+=") {
                    scope[left] = currentVal + (delta?.toString() ?: "")
                    return
                }

                val lNum = (currentVal as? Number)?.toDouble() ?: 0.0
                val rNum = (delta as? Number)?.toDouble() ?: 0.0

                val res = when (op) {
                    "+=" -> lNum + rNum
                    "-=" -> lNum - rNum
                    "*=" -> lNum * rNum
                    "/=" -> if (rNum != 0.0) lNum / rNum else 0.0
                    "%=" -> if (rNum != 0.0) lNum % rNum else 0.0
                    "**=" -> lNum.pow(rNum)
                    else -> lNum
                }

                scope[left] = if (res == res.toInt().toDouble()) res.toInt() else res
                return
            }
        }

        // 4. Subscript assignment: list[idx] = val or dict[key] = val
        if (trimmed.contains("=") && !trimmed.contains("==") && !trimmed.contains("<=") && !trimmed.contains(">=") && !trimmed.contains("!=")) {
            val left = trimmed.substringBefore("=").trim()
            val right = trimmed.substringAfter("=").trim()

            if (left.contains("[") && left.endsWith("]")) {
                val targetVar = left.substringBefore("[").trim()
                val indexExpr = left.substringAfter("[").substringBeforeLast("]").trim()
                val idxOrKey = evaluateExpression(indexExpr, scope)
                val value = evaluateExpression(right, scope)

                val target = scope[targetVar]
                if (target is MutableList<*>) {
                    @Suppress("UNCHECKED_CAST")
                    val list = target as MutableList<Any?>
                    val i = (idxOrKey as? Number)?.toInt() ?: 0
                    val realIdx = if (i < 0) list.size + i else i
                    if (realIdx in 0 until list.size) {
                        list[realIdx] = value
                    }
                } else if (target is MutableMap<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    (target as MutableMap<Any?, Any?>)[idxOrKey] = value
                }
                return
            }

            // Normal variable assignment: x = 10 or multiple assign a, b = 1, 2
            if (left.contains(",")) {
                val vars = left.split(",").map { it.trim() }
                val evalRight = evaluateExpression(right, scope)
                if (evalRight is List<*>) {
                    for ((vIdx, vName) in vars.withIndex()) {
                        if (vIdx < evalRight.size) {
                            scope[vName] = evalRight[vIdx]
                        }
                    }
                }
            } else {
                val value = evaluateExpression(right, scope)
                scope[left] = value
            }
            return
        }

        // 5. Method calls or direct expression evaluations (e.g. arr.append(x), time.sleep(1))
        evaluateExpression(trimmed, scope)
    }

    private suspend fun evaluatePrintArguments(argString: String, scope: MutableMap<String, Any?>): String {
        if (argString.isBlank()) return ""
        val args = splitArguments(argString)
        val sb = StringBuilder()
        for ((idx, arg) in args.withIndex()) {
            if (idx > 0) sb.append(" ")
            val evaluated = evaluateExpression(arg.trim(), scope)
            sb.append(formatPythonValue(evaluated))
        }
        return sb.toString()
    }

    private fun splitArguments(argStr: String): List<String> {
        val list = mutableListOf<String>()
        var depth = 0
        var inQuote = false
        var quoteChar = ' '
        var current = StringBuilder()

        for (c in argStr) {
            if (inQuote) {
                current.append(c)
                if (c == quoteChar) inQuote = false
            } else {
                when (c) {
                    '"', '\'' -> {
                        inQuote = true
                        quoteChar = c
                        current.append(c)
                    }
                    '(', '[', '{' -> {
                        depth++
                        current.append(c)
                    }
                    ')', ']', '}' -> {
                        depth--
                        current.append(c)
                    }
                    ',' -> {
                        if (depth == 0) {
                            list.add(current.toString())
                            current = StringBuilder()
                        } else {
                            current.append(c)
                        }
                    }
                    else -> current.append(c)
                }
            }
        }
        if (current.isNotEmpty()) {
            list.add(current.toString())
        }
        return list
    }

    private suspend fun evaluateExpression(expr: String, scope: MutableMap<String, Any?>): Any? {
        val trimmed = expr.trim()
        if (trimmed.isEmpty()) return null

        // Literals
        if (trimmed == "True") return true
        if (trimmed == "False") return false
        if (trimmed == "None") return null

        // String literals
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length - 1)
        }

        // Formatted strings f"..." or f'...'
        if ((trimmed.startsWith("f\"") && trimmed.endsWith("\"")) || (trimmed.startsWith("f'") && trimmed.endsWith("'"))) {
            val template = trimmed.substring(2, trimmed.length - 1)
            return interpolateFString(template, scope)
        }

        // Numbers
        trimmed.toIntOrNull()?.let { return it }
        trimmed.toLongOrNull()?.let { return it }
        trimmed.toDoubleOrNull()?.let { return it }

        // List literal [1, 2, 3] or comprehension
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            val inner = trimmed.substring(1, trimmed.length - 1).trim()
            if (inner.isEmpty()) return mutableListOf<Any?>()

            if (inner.contains(" for ") && inner.contains(" in ")) {
                return evaluateListComprehension(inner, scope)
            }
            return splitArguments(inner).map { evaluateExpression(it, scope) }.toMutableList()
        }

        // Dict literal {"a": 1} or comprehension
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            val inner = trimmed.substring(1, trimmed.length - 1).trim()
            if (inner.isEmpty()) return mutableMapOf<Any?, Any?>()

            if (inner.contains(" for ") && inner.contains(" in ")) {
                return evaluateDictComprehension(inner, scope)
            }

            val map = mutableMapOf<Any?, Any?>()
            val entries = splitArguments(inner)
            for (entry in entries) {
                val k = evaluateExpression(entry.substringBefore(":").trim(), scope)
                val v = evaluateExpression(entry.substringAfter(":").trim(), scope)
                map[k] = v
            }
            return map
        }

        // Builtin functions & Type casting
        if (trimmed.startsWith("int(") && trimmed.endsWith(")")) {
            val inner = evaluateExpression(trimmed.substring(4, trimmed.length - 1), scope)
            return when (inner) {
                is Number -> inner.toInt()
                is Boolean -> if (inner) 1 else 0
                is String -> inner.trim().toIntOrNull() ?: inner.trim().toDoubleOrNull()?.toInt() ?: 0
                else -> 0
            }
        }

        if (trimmed.startsWith("float(") && trimmed.endsWith(")")) {
            val inner = evaluateExpression(trimmed.substring(6, trimmed.length - 1), scope)
            return when (inner) {
                is Number -> inner.toDouble()
                is Boolean -> if (inner) 1.0 else 0.0
                is String -> inner.trim().toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
        }

        if (trimmed.startsWith("str(") && trimmed.endsWith(")")) {
            val inner = evaluateExpression(trimmed.substring(4, trimmed.length - 1), scope)
            return formatPythonValue(inner)
        }

        if (trimmed.startsWith("bool(") && trimmed.endsWith(")")) {
            val inner = evaluateExpression(trimmed.substring(5, trimmed.length - 1), scope)
            return when (inner) {
                null -> false
                is Boolean -> inner
                is Number -> inner.toDouble() != 0.0
                is String -> inner.isNotEmpty()
                is Collection<*> -> inner.isNotEmpty()
                else -> true
            }
        }

        if (trimmed.startsWith("abs(") && trimmed.endsWith(")")) {
            val inner = (evaluateExpression(trimmed.substring(4, trimmed.length - 1), scope) as? Number)?.toDouble() ?: 0.0
            return if (abs(inner) == abs(inner).toInt().toDouble()) abs(inner).toInt() else abs(inner)
        }

        if (trimmed.startsWith("range(") && trimmed.endsWith(")")) {
            val args = splitArguments(trimmed.substring(6, trimmed.length - 1))
            return when (args.size) {
                1 -> {
                    val stop = (evaluateExpression(args[0], scope) as? Number)?.toInt() ?: 0
                    (0 until stop).toList()
                }
                2 -> {
                    val start = (evaluateExpression(args[0], scope) as? Number)?.toInt() ?: 0
                    val stop = (evaluateExpression(args[1], scope) as? Number)?.toInt() ?: 0
                    (start until stop).toList()
                }
                else -> emptyList<Int>()
            }
        }

        if (trimmed.startsWith("len(") && trimmed.endsWith(")")) {
            val target = evaluateExpression(trimmed.substring(4, trimmed.length - 1), scope)
            return when (target) {
                is Collection<*> -> target.size
                is String -> target.length
                is Map<*, *> -> target.size
                else -> 0
            }
        }

        if (trimmed.startsWith("sum(") && trimmed.endsWith(")")) {
            val target = evaluateExpression(trimmed.substring(4, trimmed.length - 1), scope)
            return if (target is Collection<*>) {
                val s = target.filterIsInstance<Number>().sumOf { it.toDouble() }
                if (s == s.toInt().toDouble()) s.toInt() else s
            } else 0
        }

        if (trimmed.startsWith("min(") && trimmed.endsWith(")")) {
            val target = evaluateExpression(trimmed.substring(4, trimmed.length - 1), scope)
            return (target as? Collection<*>)?.filterIsInstance<Comparable<Any>>()?.minOrNull()
        }

        if (trimmed.startsWith("max(") && trimmed.endsWith(")")) {
            val target = evaluateExpression(trimmed.substring(4, trimmed.length - 1), scope)
            return (target as? Collection<*>)?.filterIsInstance<Comparable<Any>>()?.maxOrNull()
        }

        if (trimmed.startsWith("round(") && trimmed.endsWith(")")) {
            val args = splitArguments(trimmed.substring(6, trimmed.length - 1))
            val num = (evaluateExpression(args[0], scope) as? Number)?.toDouble() ?: 0.0
            val decimals = if (args.size > 1) (evaluateExpression(args[1], scope) as? Number)?.toInt() ?: 0 else 0
            val factor = 10.0.pow(decimals)
            return kotlin.math.round(num * factor) / factor
        }

        if (trimmed.startsWith("enumerate(") && trimmed.endsWith(")")) {
            val target = evaluateExpression(trimmed.substring(10, trimmed.length - 1), scope)
            val list = (target as? List<*>) ?: (target as? Collection<*>)?.toList() ?: emptyList()
            return list.mapIndexed { idx, v -> listOf(idx, v) }
        }

        // Math module calls
        if (trimmed.startsWith("math.sqrt(") && trimmed.endsWith(")")) {
            val num = (evaluateExpression(trimmed.substring(10, trimmed.length - 1), scope) as? Number)?.toDouble() ?: 0.0
            val res = sqrt(num)
            return if (res == res.toInt().toDouble()) res.toInt() else res
        }
        if (trimmed.startsWith("math.sin(") && trimmed.endsWith(")")) {
            val num = (evaluateExpression(trimmed.substring(9, trimmed.length - 1), scope) as? Number)?.toDouble() ?: 0.0
            return sin(num)
        }
        if (trimmed.startsWith("math.cos(") && trimmed.endsWith(")")) {
            val num = (evaluateExpression(trimmed.substring(9, trimmed.length - 1), scope) as? Number)?.toDouble() ?: 0.0
            return cos(num)
        }
        if (trimmed.startsWith("math.floor(") && trimmed.endsWith(")")) {
            val num = (evaluateExpression(trimmed.substring(11, trimmed.length - 1), scope) as? Number)?.toDouble() ?: 0.0
            return floor(num).toInt()
        }
        if (trimmed.startsWith("math.ceil(") && trimmed.endsWith(")")) {
            val num = (evaluateExpression(trimmed.substring(10, trimmed.length - 1), scope) as? Number)?.toDouble() ?: 0.0
            return ceil(num).toInt()
        }
        if (trimmed == "math.pi") return Math.PI
        if (trimmed == "math.e") return Math.E

        // Random module calls
        if (trimmed.startsWith("random.randint(") && trimmed.endsWith(")")) {
            val args = splitArguments(trimmed.substring(15, trimmed.length - 1))
            val min = (evaluateExpression(args[0], scope) as? Number)?.toInt() ?: 0
            val max = (evaluateExpression(args[1], scope) as? Number)?.toInt() ?: 10
            return Random.nextInt(min, max + 1)
        }
        if (trimmed.startsWith("random.choice(") && trimmed.endsWith(")")) {
            val target = evaluateExpression(trimmed.substring(14, trimmed.length - 1), scope) as? List<*>
            return target?.randomOrNull()
        }
        if (trimmed == "random.random()") {
            return Random.nextDouble()
        }

        // Time module
        if (trimmed == "time.time()") {
            return System.currentTimeMillis() / 1000.0
        }
        if (trimmed.startsWith("time.sleep(") && trimmed.endsWith(")")) {
            val sec = (evaluateExpression(trimmed.substring(11, trimmed.length - 1), scope) as? Number)?.toDouble() ?: 0.0
            delay((sec * 1000).toLong().coerceAtLeast(0L))
            return null
        }

        // Subscript access on variables e.g. sieve[i], arr[len(arr) // 2], fib[-1]
        if (trimmed.contains("[") && trimmed.endsWith("]")) {
            val objExpr = trimmed.substringBefore("[")
            val indexExpr = trimmed.substringAfter("[").substringBeforeLast("]")
            val obj = evaluateExpression(objExpr, scope)
            val indexVal = evaluateExpression(indexExpr, scope)

            if (obj is List<*>) {
                val idx = (indexVal as? Number)?.toInt() ?: 0
                val realIdx = if (idx < 0) obj.size + idx else idx
                if (realIdx in 0 until obj.size) {
                    return obj[realIdx]
                }
            } else if (obj is String) {
                val idx = (indexVal as? Number)?.toInt() ?: 0
                val realIdx = if (idx < 0) obj.length + idx else idx
                if (realIdx in 0 until obj.length) {
                    return obj[realIdx].toString()
                }
            } else if (obj is Map<*, *>) {
                return obj[indexVal]
            }
        }

        // Method calls on object e.g. arr.append(x), str.upper()
        if (trimmed.contains(".") && trimmed.contains("(") && trimmed.endsWith(")")) {
            val targetExpr = trimmed.substringBeforeLast(".")
            val methodCall = trimmed.substringAfterLast(".")
            val methodName = methodCall.substringBefore("(")
            val argStr = methodCall.substringAfter("(").substringBeforeLast(")")
            val target = evaluateExpression(targetExpr, scope)

            if (target is MutableList<*>) {
                @Suppress("UNCHECKED_CAST")
                val list = target as MutableList<Any?>
                when (methodName) {
                    "append" -> {
                        val item = evaluateExpression(argStr, scope)
                        list.add(item)
                        return null
                    }
                    "extend" -> {
                        val item = evaluateExpression(argStr, scope)
                        if (item is List<*>) list.addAll(item)
                        return null
                    }
                    "pop" -> {
                        return if (list.isNotEmpty()) list.removeAt(list.lastIndex) else null
                    }
                    "clear" -> {
                        list.clear()
                        return null
                    }
                }
            } else if (target is String) {
                when (methodName) {
                    "upper" -> return target.uppercase()
                    "lower" -> return target.lowercase()
                    "strip" -> return target.trim()
                    "split" -> {
                        val sep = if (argStr.isNotBlank()) evaluateExpression(argStr, scope)?.toString() ?: " " else " "
                        return target.split(sep).toMutableList()
                    }
                }
            }
        }

        // Binary Arithmetic operators e.g. a + b, a * b, a / b, a - b, a ** b
        val binaryOps = listOf("+", "-", "//", "%", "/", "*", "**")
        for (op in binaryOps) {
            val opIndex = findTopLevelOperator(trimmed, op)
            if (opIndex > 0 && opIndex < trimmed.length - op.length) {
                val leftRaw = trimmed.substring(0, opIndex).trim()
                val rightRaw = trimmed.substring(opIndex + op.length).trim()
                val left = evaluateExpression(leftRaw, scope)
                val right = evaluateExpression(rightRaw, scope)

                // List concatenation
                if (op == "+" && left is List<*> && right is List<*>) {
                    val combined = mutableListOf<Any?>()
                    combined.addAll(left)
                    combined.addAll(right)
                    return combined
                }

                // String repetition: "-" * 50
                if (op == "*" && left is String && right is Number) {
                    return left.repeat(right.toInt().coerceAtLeast(0))
                }
                if (op == "*" && left is Number && right is String) {
                    return right.repeat(left.toInt().coerceAtLeast(0))
                }

                // List repetition: [True] * (limit + 1)
                if (op == "*" && left is List<*> && right is Number) {
                    val count = right.toInt().coerceAtLeast(0)
                    val resultList = mutableListOf<Any?>()
                    for (k in 0 until count) {
                        resultList.addAll(left)
                    }
                    return resultList
                }

                // String concatenation
                if (op == "+" && (left is String || right is String)) {
                    return "${left ?: ""}${right ?: ""}"
                }

                val lNum = (left as? Number)?.toDouble() ?: 0.0
                val rNum = (right as? Number)?.toDouble() ?: 0.0

                val res = when (op) {
                    "+" -> lNum + rNum
                    "-" -> lNum - rNum
                    "*" -> lNum * rNum
                    "/" -> if (rNum != 0.0) lNum / rNum else 0.0
                    "//" -> if (rNum != 0.0) floor(lNum / rNum) else 0.0
                    "%" -> if (rNum != 0.0) lNum % rNum else 0.0
                    "**" -> lNum.pow(rNum)
                    else -> 0.0
                }

                return if (res == res.toInt().toDouble()) res.toInt() else res
            }
        }

        // User defined function call
        if (trimmed.contains("(") && trimmed.endsWith(")")) {
            val funcName = trimmed.substringBefore("(").trim()
            val args = splitArguments(trimmed.substringAfter("(").substringBeforeLast(")"))
            val func = scope[funcName]
            if (func is PythonFunction) {
                val localScope = HashMap(scope)
                for ((idx, param) in func.params.withIndex()) {
                    if (idx < args.size) {
                        localScope[param] = evaluateExpression(args[idx], scope)
                    }
                }
                return executeFunction(func, localScope)
            }
        }

        // Variable lookup
        if (scope.containsKey(trimmed)) {
            return scope[trimmed]
        }

        return trimmed
    }

    private suspend fun executeFunction(func: PythonFunction, localScope: MutableMap<String, Any?>): Any? {
        var i = 0
        while (i < func.bodyLines.size) {
            val line = func.bodyLines[i]
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed == "pass") {
                i++
                continue
            }

            if (trimmed.startsWith("return ")) {
                val retExpr = trimmed.removePrefix("return ").trim()
                return evaluateExpression(retExpr, localScope)
            } else if (trimmed == "return") {
                return null
            } else {
                executeSingleStatement(trimmed, localScope)
                i++
            }
        }
        return null
    }

    private suspend fun evaluateCondition(expr: String, scope: MutableMap<String, Any?>): Boolean {
        val trimmed = expr.trim()
        if (trimmed == "True" || trimmed == "1") return true
        if (trimmed == "False" || trimmed == "0") return false

        // Logical 'and' & 'or'
        if (trimmed.contains(" and ")) {
            val left = trimmed.substringBefore(" and ").trim()
            val right = trimmed.substringAfter(" and ").trim()
            return evaluateCondition(left, scope) && evaluateCondition(right, scope)
        }
        if (trimmed.contains(" or ")) {
            val left = trimmed.substringBefore(" or ").trim()
            val right = trimmed.substringAfter(" or ").trim()
            return evaluateCondition(left, scope) || evaluateCondition(right, scope)
        }
        if (trimmed.startsWith("not ")) {
            return !evaluateCondition(trimmed.removePrefix("not ").trim(), scope)
        }

        // Comparisons
        val compOps = listOf("==", "!=", "<=", ">=", "<", ">", " in ")
        for (op in compOps) {
            if (trimmed.contains(op)) {
                val left = evaluateExpression(trimmed.substringBefore(op).trim(), scope)
                val right = evaluateExpression(trimmed.substringAfter(op).trim(), scope)

                val lNum = (left as? Number)?.toDouble()
                val rNum = (right as? Number)?.toDouble()

                return when (op) {
                    "==" -> left == right
                    "!=" -> left != right
                    "<=" -> if (lNum != null && rNum != null) lNum <= rNum else false
                    ">=" -> if (lNum != null && rNum != null) lNum >= rNum else false
                    "<" -> if (lNum != null && rNum != null) lNum < rNum else false
                    ">" -> if (lNum != null && rNum != null) lNum > rNum else false
                    " in " -> (right as? Collection<*>)?.contains(left) ?: false
                    else -> false
                }
            }
        }
        val evaluated = evaluateExpression(trimmed, scope)
        return when (evaluated) {
            is Boolean -> evaluated
            is Number -> evaluated.toDouble() != 0.0
            is String -> evaluated.isNotEmpty()
            is Collection<*> -> evaluated.isNotEmpty()
            null -> false
            else -> true
        }
    }

    private suspend fun interpolateFString(template: String, scope: MutableMap<String, Any?>): String {
        val regex = Regex("\\{([^}]+)\\}")
        val matches = regex.findAll(template).toList()
        if (matches.isEmpty()) return template

        val sb = StringBuilder()
        var lastIndex = 0
        for (match in matches) {
            sb.append(template.substring(lastIndex, match.range.first))
            val exprWithFormat = match.groupValues[1]
            val expr = exprWithFormat.substringBefore(":")
            val formatSpec = if (exprWithFormat.contains(":")) exprWithFormat.substringAfter(":") else null

            val res = evaluateExpression(expr, scope)
            val formatted = if (formatSpec != null && res is Number) {
                if (formatSpec.endsWith("f")) {
                    val decimals = formatSpec.removeSuffix("f").removePrefix(".").toIntOrNull() ?: 2
                    String.format(Locale.US, "%.${decimals}f", res.toDouble())
                } else if (formatSpec.endsWith("d")) {
                    res.toInt().toString()
                } else {
                    res.toString()
                }
            } else {
                formatPythonValue(res)
            }
            sb.append(formatted)
            lastIndex = match.range.last + 1
        }
        sb.append(template.substring(lastIndex))
        return sb.toString()
    }

    private suspend fun evaluateListComprehension(comp: String, scope: MutableMap<String, Any?>): List<Any?> {
        val outExpr = comp.substringBefore(" for ").trim()
        val rest = comp.substringAfter(" for ").trim()
        val varName = rest.substringBefore(" in ").trim()
        val iterExpr = rest.substringAfter(" in ").substringBefore(" if ").trim()
        val ifExpr = if (rest.contains(" if ")) rest.substringAfter(" if ").trim() else null

        val iterable = evaluateExpression(iterExpr, scope)
        val items = (iterable as? Collection<*>) ?: emptyList<Any?>()
        val result = mutableListOf<Any?>()

        for (item in items) {
            val localScope = HashMap(scope)
            if (varName.contains(",")) {
                val parts = varName.split(",").map { it.trim() }
                if (item is List<*> && item.size >= parts.size) {
                    for ((pIdx, pName) in parts.withIndex()) {
                        localScope[pName] = item[pIdx]
                    }
                }
            } else {
                localScope[varName] = item
            }

            if (ifExpr == null || evaluateCondition(ifExpr, localScope)) {
                result.add(evaluateExpression(outExpr, localScope))
            }
        }
        return result
    }

    private suspend fun evaluateDictComprehension(comp: String, scope: MutableMap<String, Any?>): Map<Any?, Any?> {
        val keyExpr = comp.substringBefore(":").trim()
        val valExpr = comp.substringAfter(":").substringBefore(" for ").trim()
        val rest = comp.substringAfter(" for ").trim()
        val varName = rest.substringBefore(" in ").trim()
        val iterExpr = rest.substringAfter(" in ").substringBefore(" if ").trim()

        val iterable = evaluateExpression(iterExpr, scope)
        val items = (iterable as? Collection<*>) ?: emptyList<Any?>()
        val result = mutableMapOf<Any?, Any?>()

        for (item in items) {
            val localScope = HashMap(scope)
            localScope[varName] = item
            val k = evaluateExpression(keyExpr, localScope)
            val v = evaluateExpression(valExpr, localScope)
            result[k] = v
        }
        return result
    }

    private fun findTopLevelOperator(expr: String, op: String): Int {
        var depth = 0
        var inQuote = false
        var quoteChar = ' '
        var i = 0

        while (i < expr.length) {
            val c = expr[i]
            if (inQuote) {
                if (c == quoteChar) inQuote = false
            } else {
                when (c) {
                    '"', '\'' -> {
                        inQuote = true
                        quoteChar = c
                    }
                    '(', '[', '{' -> depth++
                    ')', ']', '}' -> depth--
                    else -> {
                        if (depth == 0 && expr.startsWith(op, i)) {
                            // Prevent '+' from matching '++'
                            if (op == "+" && ((i > 0 && expr[i - 1] == '+') || (i + 1 < expr.length && expr[i + 1] == '+'))) {
                                i++
                                continue
                            }
                            // Prevent '-' from matching '--' or exponent 'e-'
                            if (op == "-" && (i > 0 && (expr[i - 1] == '-' || expr[i - 1] == 'e' || expr[i - 1] == 'E'))) {
                                i++
                                continue
                            }
                            // Prevent '*' from matching '**'
                            if (op == "*" && ((i > 0 && expr[i - 1] == '*') || (i + 1 < expr.length && expr[i + 1] == '*'))) {
                                i++
                                continue
                            }
                            // Prevent '/' from matching '//'
                            if (op == "/" && ((i > 0 && expr[i - 1] == '/') || (i + 1 < expr.length && expr[i + 1] == '/'))) {
                                i++
                                continue
                            }
                            return i
                        }
                    }
                }
            }
            i++
        }
        return -1
    }

    private fun formatPythonValue(v: Any?): String {
        return when (v) {
            null -> "None"
            is Boolean -> if (v) "True" else "False"
            is String -> v
            is List<*> -> "[${v.joinToString(", ") { formatPythonValue(it) }}]"
            is Map<*, *> -> "{${v.entries.joinToString(", ") { "${formatPythonValue(it.key)}: ${formatPythonValue(it.value)}" }}}"
            is Double -> if (v == v.toInt().toDouble()) v.toInt().toString() else v.toString()
            else -> v.toString()
        }
    }
}
