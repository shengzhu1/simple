package top.androider.util

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

private val LINE_SEP = System.getProperty("line.separator")


/**
 * Execute the command asynchronously.
 *
 * @param command         The command.
 * @param isRooted        True to use root, false otherwise.
 * @param isNeedResultMsg True to return the message of result, false otherwise.
 * @param consumer        The consumer.
 * @return the task
 */
fun String.execCmdAsync(
    isRooted: Boolean,
    isNeedResultMsg: Boolean = true,
    consumer: Utils.Consumer<CommandResult>
): Utils.Task<CommandResult> {
    return arrayOf(this).execCmdAsync(isRooted, isNeedResultMsg, consumer)
}

/**
 * Execute the command asynchronously.
 *
 * @param commands        The commands.
 * @param isRooted        True to use root, false otherwise.
 * @param isNeedResultMsg True to return the message of result, false otherwise.
 * @param consumer        The consumer.
 * @return the task
 */
fun List<String>?.execCmdAsync(
    isRooted: Boolean,
    isNeedResultMsg: Boolean = true,
    consumer: Utils.Consumer<CommandResult>
): Utils.Task<CommandResult> {
    return this?.toTypedArray().execCmdAsync(
        isRooted,
        isNeedResultMsg,
        consumer
    )
}

/**
 * Execute the command asynchronously.
 *
 * @param commands        The commands.
 * @param isRooted        True to use root, false otherwise.
 * @param isNeedResultMsg True to return the message of result, false otherwise.
 * @param consumer        The consumer.
 * @return the task
 */
fun Array<String>?.execCmdAsync(
    isRooted: Boolean,
    isNeedResultMsg:Boolean = true,
    consumer: Utils.Consumer<CommandResult>
): Utils.Task<CommandResult> {
    return doAsync<CommandResult>(object : Utils.Task<CommandResult>(consumer) {
        override fun doInBackground(): CommandResult {
            return this@execCmdAsync.execCmd(isRooted, isNeedResultMsg = isNeedResultMsg)
        }
    })
}

/**
 * Execute the command.
 *
 * @param command         The command.
 * @param isRooted        True to use root, false otherwise.
 * @param isNeedResultMsg True to return the message of result, false otherwise.
 * @return the single [CommandResult] instance
 */
fun String.execCmd(
    isRooted: Boolean,
    isNeedResultMsg: Boolean = true
): CommandResult {
    return arrayOf(this).execCmd( isRooted, isNeedResultMsg)
}

/**
 * Execute the command.
 *
 * @param commands        The commands.
 * @param isRooted        True to use root, false otherwise.
 * @param isNeedResultMsg True to return the message of result, false otherwise.
 * @return the single [CommandResult] instance
 */
fun List<String>?.execCmd(
    isRooted: Boolean,
    isNeedResultMsg: Boolean = true
): CommandResult {
    return this?.toTypedArray().execCmd(
        isRooted,
        isNeedResultMsg
    )
}

/**
 * Execute the command.
 *
 * @param commands The commands.
 * @param isRooted True to use root, false otherwise.
 * @return the single [CommandResult] instance
 */
@JvmOverloads
fun Array<String>?.execCmd(
    isRooted: Boolean,
    isNeedResultMsg: Boolean = true
): CommandResult {
    var result = -1
    if (this?.size?:0 == 0) {
        return CommandResult(result, "", "")
    }
    var process: Process? = null
    var successResult: BufferedReader? = null
    var errorResult: BufferedReader? = null
    var successMsg: StringBuilder? = null
    var errorMsg: StringBuilder? = null
    var os: DataOutputStream? = null
    try {
        process = Runtime.getRuntime().exec(if (isRooted) "su" else "sh")
        os = DataOutputStream(process.outputStream)
        for (command:String? in this!!) {
            command?.apply {
                os.write(command.toByteArray())
                os.writeBytes(LINE_SEP)
                os.flush()
            }
        }
        os.writeBytes("exit" + LINE_SEP)
        os.flush()
        result = process.waitFor()
        if (isNeedResultMsg) {
            successMsg = StringBuilder()
            errorMsg = StringBuilder()
            successResult = BufferedReader(
                InputStreamReader(process.inputStream, "UTF-8")
            )
            errorResult = BufferedReader(
                InputStreamReader(process.errorStream, "UTF-8")
            )
            var line: String?
            if (successResult.readLine().also { line = it } != null) {
                successMsg.append(line)
                while (successResult.readLine().also { line = it } != null) {
                    successMsg.append(LINE_SEP).append(line)
                }
            }
            if (errorResult.readLine().also { line = it } != null) {
                errorMsg.append(line)
                while (errorResult.readLine().also { line = it } != null) {
                    errorMsg.append(LINE_SEP).append(line)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            os?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            successResult?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            errorResult?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        process?.destroy()
    }
    return CommandResult(
        result,
        successMsg?.toString() ?: "",
        errorMsg?.toString() ?: ""
    )
}

/**
 * The result of command.
 */
class CommandResult(var result: Int, var successMsg: String, var errorMsg: String) {
    override fun toString(): String {
        return """
                  result: $result
                  successMsg: $successMsg
                  errorMsg: $errorMsg
                  """.trimIndent()
    }
}