import api.UserSession
import api.repo.NoodleRepository
import api.routeApi
import dao.DatabaseFactory
import dao.SESSION_EXPIRE_AFTER_SECONDS
import dao.SessionDAOFacadeImpl
import dao.UserDAOFacadeImpl
import io.ktor.http.*
import io.ktor.network.tls.certificates.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.httpsredirect.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.sessions.serialization.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import utils.ServerProperty
import java.io.File
import java.net.URI
import java.security.KeyStore

fun debugEnvironmentConfig(): ApplicationEngineEnvironment {
    val keyStoreFile = File("build/keystore.jks")

    val keyStore = if (keyStoreFile.exists()){
        println("Load from ${keyStoreFile.absolutePath}")
        KeyStore.getInstance(keyStoreFile,"123456".toCharArray())
    }else buildKeyStore {
        certificate("sampleAlias") {
            password = "foobar"
            domains = listOf("127.0.0.1", "0.0.0.0", "localhost")
            daysValid=365*15
        }
    }.apply {
        saveToFile(keyStoreFile, "123456")
    }

    return applicationEngineEnvironment {
       // developmentMode = true
        connector {
            port = 8080
        }
        module(Application::myApplicationModule)
        sslConnector(
            keyStore = keyStore,
            keyAlias = "sampleAlias",
            keyStorePassword = { "123456".toCharArray() },
            privateKeyPassword = { "foobar".toCharArray() }) {
            port = 8443
            keyStorePath = keyStoreFile
        }
    }
}

fun main() {
    embeddedServer(Netty, debugEnvironmentConfig()).start(true)
    //embeddedServer(Netty, 8080, module = Application::myApplicationModule, ).start(wait = true)
}

private val appModule = module {
    single {
        ServerProperty()
    }
    single {
        val s3EndPoint by it.get<ServerProperty>()
        val s3AccessKeyId by it.get<ServerProperty>()
        val s3AccessKeySecret by it.get<ServerProperty>()
        val s3PathStyle by it.get<ServerProperty>()
        S3Client.builder().credentialsProvider {
            AwsBasicCredentials.create(s3AccessKeyId, s3AccessKeySecret)
        }.endpointOverride(URI.create(s3EndPoint)).region(Region.of("auto"))
            .forcePathStyle(s3PathStyle.toBooleanStrict() ?: false).build()
    }
    single {
        NoodleRepository()
    }
    single {
        UserDAOFacadeImpl()
    }
    single {
        SessionDAOFacadeImpl()
    }
}

val redirects = mutableMapOf<String, String>()
const val GITHUB_OAUTH = "auth-oauth-github"
const val SESSION_AUTH = "auth-session"
const val JWT_AUTH = "auth-jwt"
fun Application.myApplicationModule() {

    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    DatabaseFactory.init()
    install(HttpsRedirect) {
        sslPort = 8443
        permanentRedirect = true
    }
    install(CallLogging) {
        level = Level.INFO
    }
    val sessionEncryptKey by get<ServerProperty>()
    val sessionSignKey by get<ServerProperty>()
    install(Sessions) {
        val secretEncryptKey = hex(sessionEncryptKey)
        val secretSignKey = hex(sessionSignKey)
        cookie<UserSession>(SESSION_COOKIE) {
            serializer = KotlinxSessionSerializer<UserSession>(Json)
            cookie.path = "/"
            cookie.maxAgeInSeconds = SESSION_EXPIRE_AFTER_SECONDS
            cookie.httpOnly = true
            //TODO cookie.secure=true
            transform(SessionTransportTransformerEncrypt(secretEncryptKey, secretSignKey))
        }
    }
    install(Resources)
    install(ContentNegotiation) {
        json()

    }
    install(CORS) {
        allowMethod(HttpMethod.Get)
        anyHost()
    }
    install(Compression) {
        gzip()
    }


    val githubClientId by get<ServerProperty>()
    val githubClientSecret by get<ServerProperty>()
    val sessionDAO = get<SessionDAOFacadeImpl>()
    authentication {
        session<UserSession>(SESSION_AUTH) {
            validate { it ->
                if (sessionDAO.session(it.sessionId)?.isValid() == true) {
                    it
                } else {
                    //sessionDAO.deleteSession(it.sessionId)
                    null
                }.also {
                    println(it)
                }

            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Session is invalid")
            }
        }
        /*jwt(JWT_AUTH) {
            realm=JWTFactory.jwtRealm
            verifier(JWTFactory.verifier())
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }*/
        oauth(GITHUB_OAUTH) {
            urlProvider = { "http://127.0.0.1:8080/api/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "github",
                    authorizeUrl = "https://github.com/login/oauth/authorize",
                    accessTokenUrl = "https://github.com/login/oauth/access_token",
                    requestMethod = HttpMethod.Post,
                    clientId = githubClientId,
                    clientSecret = githubClientSecret,
                    defaultScopes = listOf("user"),
                    onStateCreated = { call, state ->
                        redirects[state] = call.request.queryParameters["redirectUrl"]!!
                    }
                )
            }
            client = httpClient
        }
    }
    routing {
        routeApi()
        singlePageApplication {
            useResources = true
            react("/")
        }
    }
}

