package com.task.appChecker

import org.jetbrains.exposed.dao.IntIdTable

object Attempts: IntIdTable() {
    val date = datetime("date")
    val name = varchar("name", 256)
    val src = varchar("src", 4096)
    val status = enumeration("status", klass = AttemptStatus::class)
    val score = short("score")
    val compilerOutput = varchar("compilerOutput", 4096)
    val testOutput = varchar("testOutput", 4096)
}