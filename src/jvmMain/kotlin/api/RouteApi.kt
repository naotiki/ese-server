package api

import GITHUB_OAUTH
import SESSION_AUTH
import api.s3repo.Noodle
import api.s3repo.NoodleS3Repository
import dao.*
import dao.DatabaseFactory.dbQuery
import data.CreateNoodleRepositoryData
import httpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.get as resourceGet
import io.ktor.server.resources.post as resourcePost
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.async
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.User
import org.koin.ktor.ext.inject
import redirects
import java.util.*

@Serializable
data class PartialGitHubUser(
    @SerialName("login")
    val userName: String,
    val id: Long,
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("name")
    val displayName: String
)

@Serializable
data class GitHubEmail(
    val email: String,
    val primary: Boolean,
    val verified: Boolean,
    val visibility: String?
)

fun HttpRequestBuilder.headerBearerToken(token: String) = header(HttpHeaders.Authorization, "Bearer $token")
internal fun Routing.routeApi() {
    val ndlRepo by inject<NoodleS3Repository>()

    val userDAO by inject<UserDAOFacadeImpl>()
    val sessionDAO by inject<SessionDAOFacadeImpl>()
    val noodleDAO by inject<NoodleDAOFacadeImpl>()
    route("/api") {

        authenticate(GITHUB_OAUTH) {
            get("/login") { }
            get("/callback") {

                val userSession = call.sessions.get<UserSession>()
                val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                if (principal?.accessToken == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }
                val userRequest = async {
                    httpClient.get("https://api.github.com/user") {
                        header(HttpHeaders.Accept, "application/vnd.github+json")
                        headerBearerToken(principal.accessToken)
                        header("X-GitHub-Api-Version", "2022-11-28")
                    }.body<PartialGitHubUser>()
                }
                val emailRequest = async {
                    httpClient.get("https://api.github.com/user/emails") {
                        header(HttpHeaders.Accept, "application/vnd.github+json")
                        headerBearerToken(principal.accessToken)
                        header("X-GitHub-Api-Version", "2022-11-28")
                    }.body<List<GitHubEmail>>()
                }
                val user = userRequest.await()
                val email = emailRequest.await()
                val dbUser = userDAO.userByGitHub(user.id) ?: userDAO.addUser(
                    user.id,
                    user.userName,
                    email.single { it.primary }.email,
                    principal.accessToken
                )
                println("CALLBACK   " + userSession)
                val s = if (userSession == null) {
                    sessionDAO.addSession(dbUser)
                } else {
                    sessionDAO.session(userSession.sessionId)?.let {
                        if (it.isValid()) {
                            //有効期限
                            dbQuery {
                                it.expiredAt = System.currentTimeMillis() + SESSION_EXPIRE_AFTER
                                it
                            }
                        } else null
                    } ?: sessionDAO.addSession(dbUser)
                }.toUserSession()

                call.sessions.set(s)
                //call.response.header(HttpHeaders.SetCookie, "token=$jwt; Max-Age=3600")

                call.respondRedirect(redirects[principal.state!!]!!)
            }

        }

        authenticate(SESSION_AUTH) {
            get("/hello") {
                val userSession = call.principal<UserSession>()
                if (userSession != null) {
                    val s = sessionDAO.userBySession(userSession.sessionId)

                    call.respondText(s?.toPartialUser().toString())

                } else call.respondText("null")
            }
            get("/logout") {
                val userSession = call.principal<UserSession>()
                if (userSession != null) {
                    val sessionDeleted = sessionDAO.deleteSession(userSession.sessionId)
                    if (sessionDeleted) {
                        call.sessions.clear<UserSession>()

                        call.respondRedirect(  call.request.queryParameters["redirectUrl"]?:"/")
                        return@get
                    }
                }
                call.respond(HttpStatusCode.Unauthorized, "Unauthorized / 認証されていません。")
            }

            route("/user") {
                get {
                    val userSession = call.principal<UserSession>()
                    if (userSession != null) {
                        val s = sessionDAO.userBySession(userSession.sessionId)
                        if (s != null) {
                            call.respond(s.toPartialUser())
                            return@get
                        }
                    }
                    call.respond(HttpStatusCode.Unauthorized)
                }
                route("/noodleRepositories") {
                    get {
                        userByUserSession(sessionDAO) { user ->
                            call.respond(noodleDAO.userRepositories(user.id.value).map { it.toNoodleRepoData() })
                        }
                    }
                    put {
                        userByUserSession(sessionDAO) { user ->
                            val (name, description, url) = call.receive<CreateNoodleRepositoryData>()

                            call.respond(noodleDAO.addRepository(user, name, description.ifEmpty { null },
                                url.ifEmpty { null }).toNoodleRepoData()
                            )
                        }

                    }
                    get("/checkName") {
                        userByUserSession(sessionDAO) {
                            val name = call.request.queryParameters["name"].toString()
                            call.respond(
                                noodleDAO.checkNoodleRepoName(name, it)
                            )
                        }
                    }

                }
            }
        }
        authenticate(SESSION_AUTH, optional = true){
            route("/users/{user}") {
                get {
                    val paramUser=userDAO.userByName(call.parameters["user"]!!)!!
                    val u=userByUserSession(sessionDAO)
                    call.respond(if (paramUser.name==u?.name) {
                        paramUser.toPartialUserWithDetail()
                    }else paramUser.toPartialUser())
                }
                get("/noodleRepositories") {
                    val paramUser=userDAO.userByName(call.parameters["user"]!!)!!
                    call.respond(noodleDAO.userRepositories(paramUser.id.value).map { it.toNoodleRepoData() })
                }
                route("/{noodleRepo}") {
                    get {
                        val paramUser=userDAO.userByName(call.parameters["user"]!!)!!

                        call.respond(noodleDAO.getRepoByUserAndName(paramUser,call.parameters["noodleRepo"]!!)!!.toNoodleRepoData())
                    }
                    get("/all"){
                        val paramUser=userDAO.userByName(call.parameters["user"]!!)!!

                        call.respond(noodleDAO.getNoodles(noodleDAO.getRepoByUserAndName(paramUser,call.parameters["noodleRepo"]!!)!!.id.value)
                            .map { it.toNoodleData() })
                    }
                }

            }
        }
        route("/noodle") {
            resourceGet<Noodle> {
                val o = ndlRepo.getNoodleBytesOrNull(it)
                if (o == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@resourceGet
                }
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName,
                        it.name + ".ndl"
                    )
                        .toString()
                )
                call.respondOutputStream(ContentType("application", "ndl+zip")) {
                    o.transferTo(this)
                }
            }

            resourcePost<Noodle>() {
                val multipartData = call.receiveMultipart()

                if (it.version == null) {
                    call.respond(HttpStatusCode.BadRequest, "version is required")
                    return@resourcePost
                }

                multipartData.readPart()?.also { file ->
                    if (file !is PartData.FileItem) {
                        call.respond(HttpStatusCode.BadRequest, "File is invalid")
                        return@resourcePost
                    }
                    ndlRepo.putNoodleBytes(file.streamProvider().readBytes(), it)
                }
            }
            /*TODO()
            post<Noodle>() {
                 val multipartData = call.receiveMultipart()

                 if (it.version == null) {
                     call.respond(HttpStatusCode.BadRequest, "version is required")
                     return@post
                 }

                 multipartData.readPart()?.also { file ->
                     if (file !is PartData.FileItem) {
                         call.respond(HttpStatusCode.BadRequest, "File is invalid")
                         return@post
                     }
                     ndlRepo.putNoodleBytes(file.streamProvider().readBytes(), it)
                 }
                 call.respond("OK")
             }*/

        }
        catch("Invalid API URL")
    }
}

@Serializable
data class UserSession(
    @Serializable(UUIDSerializer::class)
    val sessionId: UUID
) : Principal

/**
 * このルート宛の定義されていないリクエストをすべてキャッチし、404を返します。
 */
fun Route.catch(msg: String = "Invalid URL") = route("/{...}") {
    handle {
        call.respondText(msg, status = HttpStatusCode.NotFound)
        finish()
    }
}

suspend inline fun PipelineContext<Unit, ApplicationCall>.userByUserSession(
    sessionDAOFacade: SessionDAOFacade,
    block: (User) -> Unit
) {
    val a = call.principal<UserSession>()
    val user = a?.sessionId?.let { sessionDAOFacade.userBySession(it) }
    if (user != null) {
        block(user)
    }

}

suspend inline fun PipelineContext<Unit, ApplicationCall>.userByUserSession(
    sessionDAOFacade: SessionDAOFacade
): User? {
    var u: User?=null
    userByUserSession(sessionDAOFacade) {
        u = it
    }
    return u
}