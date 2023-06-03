package dao

import dao.DatabaseFactory.dbQuery
import data.Level
import models.*
import org.jetbrains.exposed.sql.and

class NoodleDAOFacadeImpl : NoodleDAOFacade {
    override suspend fun getRepo(repoId: Long): NoodleRepository? = dbQuery {
        NoodleRepository.findById(repoId)
    }

    override suspend fun checkNoodleRepoName(repoName: String, user: User): Level = dbQuery {
        getRepoListByName(repoName).let {
            when {
                it.isEmpty() -> Level.OK
                it.any { it.user.id == user.id } -> Level.Error

                else -> Level.Warn
            }
        }

    }
    override suspend fun getRepoByUserAndName(user:User,repoName: String): NoodleRepository? = dbQuery {
         NoodleRepository.find {
            NoodleRepositories.name eq repoName and (NoodleRepositories.user eq user.id)
        }.singleOrNull()
    }
    override suspend fun getRepoListByName(repoName: String): List<NoodleRepository> = dbQuery {
        NoodleRepository.find {
            NoodleRepositories.name eq repoName
        }.toList()
    }
    override suspend fun getNoodles(noodleRepoId: Long): List<Noodle> = dbQuery{
        Noodle.find {
            Noodles.noodleRepo eq noodleRepoId
        }.toList()
    }

    override suspend fun getLatestNoodle(repoId: Long): Noodle? = dbQuery {
        getRepo(repoId)?.latestVersion
    }

    override suspend fun userRepositories(userId: Long): List<NoodleRepository> = dbQuery {
        NoodleRepository.find {
            NoodleRepositories.user eq userId
        }.toList()
    }

    override suspend fun addRepository(
        user: User, name: String, description: String?, url: String?
    ): NoodleRepository = dbQuery {
        NoodleRepository.new {
            this.user = user
            this.name = name
            this.description = description
            this.url = url

            val t = System.currentTimeMillis()
            createdAt = t
            updatedAt = t
        }
    }

    override suspend fun addNoodle(noodleRepository: NoodleRepository, version: String): Noodle = dbQuery {
        Noodle.new {
            this.noodleRepo = noodleRepository
            this.version = version
            createdAt = System.currentTimeMillis()
        }.also {
            noodleRepository.latestVersion = it
        }
    }


}