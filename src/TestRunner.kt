package com.task.appChecker

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class TestRunner {

    data class Result(val completedSuccessfully: Boolean, val score: Short, val output: String)

    fun runAllTests(jarFile: File): Result {

        try {

            val testDirectory = File("resources/Tests")

            val testFiles = testDirectory.listFiles() ?: throw Throwable("No tests found!")
            testFiles.sortBy { it.name }

            var passedCount = 0
            val testOutput = StringBuilder()

            for (i in testFiles.indices) {

                val testFile = testFiles[i]
                val error = runSingleTest(jarFile, testFile)

                if (error == null) {
                    passedCount++
                } else {
                    testOutput.appendln("${i + 1}. $error")
                }
            }

            return Result(
                true,
                (passedCount.toFloat() / testFiles.size * 100).roundToInt().toShort(),
                testOutput.toString()
            )

        } catch (e: Throwable) {
            e.printStackTrace()
            return Result(false, 0, "Testing failed unexpectedly")

        } finally {
            jarFile.parentFile.deleteRecursively()
        }
    }

    private fun runSingleTest(jarFile: File, testFile: File): String? {

        try {

            val inputLines = testFile.readLines().filter {
                it.isNotEmpty() && !it.startsWith("#")
            }

            // test parameters (see test files for details)
            val t = inputLines[0].toInt()
            val p = inputLines[1].toInt()
            val q = inputLines[2].toInt()

            val process = ProcessBuilder("java", "-jar", jarFile.name)
                .directory(jarFile.parentFile)
                .start()

            PrintWriter(process.outputStream).use {
                for (i in 3 until inputLines.size) {
                    it.println(inputLines[i])
                }
            }

            if (!process.waitFor(t.toLong(), TimeUnit.SECONDS)) {
                process.destroy()
                return "Test execution timed out"
            }

            if (process.exitValue() != 0) {
                process.errorStream.bufferedReader().use {
                    return it.readText().ifEmpty {
                        "Exit code does not indicate success"
                    }
                }
            }

            process.inputStream.bufferedReader().use {

                val outputLines = it.readLines()
                if (outputLines.size != 2)
                    return "Invalid number of items returned: ${outputLines.size}"

                val returnedP = outputLines[0].toIntOrNull()
                val returnedQ = outputLines[1].toIntOrNull()

                if (returnedP == null || returnedQ == null)
                    return "Unable to convert output to Int"

                if (p != returnedP || q != returnedQ)
                    return "Incorrect result"

                // test passed
                return null
            }

        } catch (e: Throwable) {
            val sw = StringWriter()
            PrintWriter(sw).use {
                e.printStackTrace(it)
                return sw.toString()
            }
        }
    }
}