package models

import data.PartialUser
import dao.UIDTable
import data.UserDetails
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

object Users : UIDTable() {
    val userName = text("name")

    val githubId = long("github_id").uniqueIndex()
    val githubAccessToken = varchar("github_access_token",128)
    val mailAddress=text("mail_address")

}

class User(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<User>(Users)
    var name by Users.userName
    var githubId by Users.githubId
    var githubAccessToken by Users.githubAccessToken
    var mailAddress by Users.mailAddress

    fun toPartialUser() = PartialUser(id.value,name)

    fun toPartialUserWithDetail() = PartialUser(id.value,name, UserDetails(githubId,mailAddress))
}




