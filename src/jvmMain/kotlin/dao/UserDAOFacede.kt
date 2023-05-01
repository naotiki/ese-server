package dao

import models.User

interface UserDAOFacade {
    suspend fun user(id:Long):User?
    suspend fun addUser(githubId: Long,userName: String,email:String,accessToken: String):User?
    suspend fun userByGitHub(githubId:Long):User?
    suspend fun deleteUser(id:Long):Boolean
    suspend fun editUser(id: Long, block: User.() -> Unit): Boolean
}

