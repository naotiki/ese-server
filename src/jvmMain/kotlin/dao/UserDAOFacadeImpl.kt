package dao

import dao.DatabaseFactory.dbQuery
import models.User
import models.Users

class UserDAOFacadeImpl : UserDAOFacade {
    override suspend fun user(id: Long): User? = dbQuery {
        User.findById(id)
    }

    override suspend fun addUser(githubId: Long, userName: String, email: String, accessToken: String): User = dbQuery {
        User.new {
            name=userName
            this.githubId = githubId
            mailAddress = email
            githubAccessToken = accessToken
        }
    }
    override suspend fun userByName(name:String): User? = dbQuery {
        User.find {
            Users.userName eq name
        }.singleOrNull()
    }
    override suspend fun userByGitHub(githubId: Long): User? = dbQuery {
        User.find {
            Users.githubId eq githubId
        }.singleOrNull()
    }

    override suspend fun deleteUser(id: Long): Boolean = dbQuery {
        User.findById(id)?.delete() != null
    }

    override suspend fun editUser(id: Long, block: User.() -> Unit): Boolean = dbQuery {
        User.findById(id)?.block() != null
    }
}