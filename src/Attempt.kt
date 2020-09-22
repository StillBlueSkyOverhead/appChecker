package com.task.appChecker

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Attempt(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Attempt>(
        Attempts
    )

    var date by Attempts.date
    var name by Attempts.name
    var src by Attempts.src
    var status by Attempts.status
    var score by Attempts.score
    var compilerOutput by Attempts.compilerOutput
    var testOutput by Attempts.testOutput
}