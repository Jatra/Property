package uk.co.jatra.property

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

private val GETPROP_PATTERN = Pattern.compile("\\[(.*?)]: \\[(.*?)]")

/**
 * Allows reading of system properties.
 *
 * Properties can be set using from the command line using
 *   adb shell setprop debug.XXXXX  Value
 *
 *   Use names scoped to the application to avoid clashes, eg
 *   adb shell setprop debug.com.example.appname.feature.enable true
 *
 *   readProperty("debug.com.example.appname.feature.enable")
 *
 *   These persist on the handset. not in any specific app.
 *
 */
fun readProperty(propName: String): String? {
    var value: String? = null
    var process: Process? = null
    try {
        process = ProcessBuilder().command("/system/bin/getprop")
            .redirectErrorStream(true).start()
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            val matches = GETPROP_PATTERN.matcher(line!!)
            if (matches.find() && matches.group(1) == propName) {
                value = matches.group(2)
                Log.d(TAG, "Found $value")
                break
            }
        }
    } finally {
        process?.destroy()
    }
    return value
}

fun readBooleanProperty(propName: String, defaultValue: Boolean): Boolean {
    return readProperty(propName)?.toBoolean() ?: defaultValue
}

fun readProperties(): Map<String, String> {
    var properties = mutableMapOf<String, String>()
    var process: Process? = null
    try {
        process = ProcessBuilder().command("/system/bin/getprop")
            .redirectErrorStream(true).start()
        val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            val matches = GETPROP_PATTERN.matcher(line!!)
            if (matches.find()) {
                properties[matches.group(1)] = matches.group(2)
            }
        }
    } finally {
        process?.destroy()
    }
    return properties
}

