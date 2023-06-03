package models

import dao.DatabaseFactory.dbQuery
import dao.UIDTable
import data.NoodleData
import data.NoodleRepositoryData
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Noodles : IntIdTable() {
    val version = varchar("version", 32)
    val noodleRepo = reference("noodle_repo", NoodleRepositories)
    val createdAt = long("created_at")
}

class Noodle(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Noodle>(Noodles)

    var version by Noodles.version
    var noodleRepo by NoodleRepository referencedOn Noodles.noodleRepo
    var createdAt by Noodles.createdAt
    suspend fun toNoodleData() = dbQuery {
        NoodleData(
            id.value,
            version,
            noodleRepo.id.value,
            createdAt
        )
    }
}


/**
 * Noodles
 * 名前は128Byte
 * バージョンは32Byteまで
 */
object NoodleRepositories : UIDTable() {
    val name = varchar("name", 128).index()
    val description = text("description").nullable().default(null)
    val url = text("url").nullable().default(null)
    val user = reference("user", Users)
    val latestVersion = reference("latest_version", Noodles).nullable().default(null)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

}


class NoodleRepository(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<NoodleRepository>(NoodleRepositories)

    var name by NoodleRepositories.name
    var description by NoodleRepositories.description
    var url by NoodleRepositories.url
    var user by User referencedOn NoodleRepositories.user
    var latestVersion by Noodle optionalReferencedOn NoodleRepositories.latestVersion
    var createdAt by NoodleRepositories.createdAt
    var updatedAt by NoodleRepositories.updatedAt

    suspend fun toNoodleRepoData() = dbQuery {
        NoodleRepositoryData(
            id.value,
            name,
            description,
            url,
            user.toPartialUser(),
            latestVersion?.toNoodleData(),
            createdAt,
            updatedAt
        )
    }
}

