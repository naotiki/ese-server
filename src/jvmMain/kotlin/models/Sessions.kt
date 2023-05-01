package models

import api.UserSession
import dao.DatabaseFactory.dbQuery
import dao.UIDTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object Sessions : UUIDTable() {
    val user = reference("user", Users)
    val createdAt = long("created_at")
    val expiredAt = long("expired_at")
}


class Session(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Session>(Sessions)


    var user by User referencedOn Sessions.user
    var createdAt by Sessions.createdAt
    var expiredAt by Sessions.expiredAt
    fun isValid() = System.currentTimeMillis() < expiredAt
    fun toUserSession(): UserSession = UserSession(id.value)

}

