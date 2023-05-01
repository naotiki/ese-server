package dao

import dao.DatabaseFactory.dbQuery
import models.Noodles.name
import models.User
import models.Users
import models.Users.githubAccessToken
import models.Users.mailAddress
import utils.UIDGenerator

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