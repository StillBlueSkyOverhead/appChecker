package com.task.appChecker

class StatusFormatter {
    fun format(status: AttemptStatus): String {
        return when (status) {
            AttemptStatus.COMPILING -> "Compiling..."
            AttemptStatus.COMPILATION_FAILED -> "Compilation Failed"
            AttemptStatus.RUNNING_TESTS -> "Testing..."
            AttemptStatus.TESTING_FAILED -> "Testing Failed"
            AttemptStatus.EVALUATED -> "Evaluated"
        }
    }
}