package api

import GITHUB_OAUTH
import SESSION_AUTH
import api.repo.Noodle
import api.repo.NoodleRepository
import dao.DatabaseFactory.dbQuery
import dao.SESSION_EXPIRE_AFTER
import dao.SessionDAOFacadeImpl
import dao.UUIDSerializer
import dao.UserDAOFacadeImpl
import httpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.async
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import software.amazon.awssdk.services.s3.S3Client
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
    val s3 by inject<S3Client>()
    val ndlRepo by inject<NoodleRepository>()

    val userDAO by inject<UserDAOFacadeImpl>()
    val sessionDAO by inject<SessionDAOFacadeImpl>()
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
                call.respondText("OK")
                //   call.respondRedirect(redirects[principal.state!!]!!)
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
        }

        get("/clear") {
            call.sessions.clear<UserSession>()
            call.respondText("Clear")
        }


        route("/noodles") {
            get<Noodle> {
                val o = ndlRepo.getNoodleBytesOrNull(it)
                if (o == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
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
            post<Noodle> {
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
            }
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