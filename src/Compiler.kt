package com.task.appChecker

import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.TimeUnit

class Compiler {

    data class Result(val jarFile: File?, val compilationError: String? = null)

    fun compile(src: String): Result {

        val uuid = UUID.randomUUID()
        val dir = File("$uuid")
        dir.mkdir()

        val srcFile = Paths.get(dir.path, "src.kt").toFile()
        srcFile.writeText(src)

        val jarFile = Paths.get(dir.path, "result.jar").toFile()
        var compiledSuccessfully = false

        try {
            val kotlinCompiler = if(System.getProperty("os.name").startsWith("W")) {
                "kotlinc.bat"
            } else {
                "kotlinc"
            }

            val process = ProcessBuilder(kotlinCompiler, srcFile.name, "-include-runtime", "-d", jarFile.name)
                .directory(dir)
                .start()

            if (!process.waitFor(20, TimeUnit.SECONDS)) {
                process.destroy()
                return Result(null, "Compilation timed out")
            }

            if (process.exitValue() == 0) {
                compiledSuccessfully = true
                return Result(jarFile)
            }

            val error = process.errorStream.bufferedReader().readText().ifEmpty {
                "Unknown compiler error"
            }
            return Result(null, error)

        } catch(e: Throwable) {
            e.printStackTrace()
            return Result(null, "Compilation failed unexpectedly")
        } finally {
            if (!compiledSuccessfully)
                dir.deleteRecursively()
        }
    }
}