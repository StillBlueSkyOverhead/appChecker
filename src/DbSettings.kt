package com.task.appChecker

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

// prevents memory leaks: https://github.com/JetBrains/Exposed/wiki/DataBase-and-DataSource
object DbSettings {
    val db by lazy {

        Database.connect(
            "jdbc:sqlite:data.db",
            "org.sqlite.JDBC"
        )

        // see https://github.com/JetBrains/Exposed/wiki/FAQ#q-sqlite3-fails-with-the-error-transaction-attempt-0-failed-sqlite-supports-only-transaction_serializable-and-transaction_read_uncommitted
        TransactionManager.manager.defaultIsolationLevel =
            Connection.TRANSACTION_SERIALIZABLE
    }
}