package data

import kotlinx.serialization.Serializable

@Serializable
data class PartialUser(
    val id:Long,
    val userName:String,

    val userDetails: UserDetails?=null
)
@Serializable
data class UserDetails(
    val githubId:Long,
    val email:String
)
