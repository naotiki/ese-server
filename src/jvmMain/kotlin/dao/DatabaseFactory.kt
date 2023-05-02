package dao

import kotlinx.coroutines.Dispatchers
import models.Noodles
import models.Sessions
import models.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import utils.ServerProperty

object DatabaseFactory : KoinComponent {
    val dbUser by get<ServerProperty>()
    val dbPass by get<ServerProperty>()
    private fun h2Init(): Database {
        val driverClassName = "org.h2.Driver"
        val jdbcURL = "jdbc:h2:file:./build/db"
        return Database.connect(jdbcURL, driverClassName)
    }

    private fun mariadbInit(): Database {
        val driverClassName = "com.mysql.cj.jdbc.Driver"
        val jdbcURL = "jdbc:mysql://localhost:13306/ese"
        return Database.connect(jdbcURL, driverClassName, dbUser, dbPass)
    }

    fun init() {
        val db = mariadbInit()
        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users, Noodles, Sessions)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}