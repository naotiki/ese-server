package dao

import models.Session
import models.User
import java.util.*

interface SessionDAOFacade {
    suspend fun session(id: UUID): Session?
    suspend fun addSession(user: User): Session?
    suspend fun sessionByUserID(userId:Long):List<Session>
    suspend fun deleteSession(id: UUID):Boolean
    suspend fun editSession(id: UUID, block: Session.() -> Unit): Boolean
    suspend fun deleteExpired(userId: Long): Int
    suspend fun userBySession(id: UUID): User?
}