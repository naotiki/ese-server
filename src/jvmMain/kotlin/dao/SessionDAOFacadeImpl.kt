package dao

import dao.DatabaseFactory.dbQuery
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import models.Session
import models.Sessions
import models.User
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import java.util.*

const val SESSION_EXPIRE_AFTER_SECONDS: Long = 8 * 3600  //8 Hours later...
const val SESSION_EXPIRE_AFTER = SESSION_EXPIRE_AFTER_SECONDS * 1000

class SessionDAOFacadeImpl : SessionDAOFacade {
    override suspend fun session(id: UUID): Session? = dbQuery {
        Session.findById(id)
    }

    override suspend fun userBySession(id:UUID):User?= dbQuery {
        session(id)?.user
    }

    override suspend fun addSession(user: User): Session = dbQuery {
        Session.new {
            this.user = user
            createdAt = System.currentTimeMillis()
            expiredAt = System.currentTimeMillis() + SESSION_EXPIRE_AFTER
        }
    }

    override suspend fun sessionByUserID(userId: Long): List<Session> = dbQuery {
        Session.find {
            Sessions.user eq userId
        }.toList()
    }

    override suspend fun deleteSession(id: UUID): Boolean = dbQuery {
        Session.findById(id)?.delete() != null

    }

    override suspend fun editSession(id: UUID, block: Session.() -> Unit): Boolean = dbQuery {
        Session.findById(id)?.block() != null
    }



    override suspend fun deleteExpired(userId: Long): Int = dbQuery {
        Sessions.deleteWhere {
            user eq userId and (expiredAt less System.currentTimeMillis())
        }
    }


}
@OptIn(ExperimentalSerializationApi::class)
@Serializer(UUID::class)
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}