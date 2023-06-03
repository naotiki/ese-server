package dao

import data.Level
import models.Noodle
import models.NoodleRepository
import models.User

interface NoodleDAOFacade {
    suspend fun getRepo(repoId:Long): NoodleRepository?
    suspend fun getLatestNoodle(repoId: Long): Noodle?
    suspend fun userRepositories(userId:Long):List<NoodleRepository>


    suspend fun addRepository(user: User, name: String, description: String?, url: String?):NoodleRepository
    suspend fun addNoodle(noodleRepository: NoodleRepository,version:String):Noodle


    suspend fun checkNoodleRepoName(repoName: String, user: User): Level
    suspend fun getRepoByUserAndName(user: User, repoName: String): NoodleRepository?
    suspend fun getRepoListByName(repoName: String): List<NoodleRepository>
    suspend fun getNoodles(noodleRepoId: Long): List<Noodle>
}