package data

import kotlinx.serialization.Serializable

@Serializable
data class NoodleRepositoryData(
    var id: Long,
    var name: String,
    var description: String?,
    var url: String?,
    var user: PartialUser,
    var latestVersion: NoodleData?,
    var createdAt: Long,
    var updatedAt: Long,
)

@Serializable
data class CreateNoodleRepositoryData(
    var name: String,
    var description: String,
    var url: String,
)

@Serializable
data class NoodleData(
    var id: Int,
    val version: String,
    val noodleRepo: Long,
    val createdAt: Long
)

val urlRegex = Regex(
    "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)"
)
//汎用的なもの
@Serializable
enum class Level {
    OK, Warn, Error
}