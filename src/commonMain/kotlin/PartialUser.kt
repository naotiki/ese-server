import kotlinx.serialization.Serializable

@Serializable
data class PartialUser(
    val id:Long,
    val userName:String,
    val githubId:Long,
    val email:String
)