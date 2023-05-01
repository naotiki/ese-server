import api.routeApi
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import utils.ServerProperty
import java.net.URI
fun main() {

    embeddedServer(Netty, 8080, module = Application::myApplicationModule).start(wait = true)
}
private val aooModule= module {
    single {
        ServerProperty()
    }
    single {
        val s3EndPoint by get<ServerProperty>()
        val s3AccessKeyId by get<ServerProperty>()
        val s3AccessKeySecret by get<ServerProperty>()
        val s3PathStyle by get<ServerProperty>()
        S3Client.builder().credentialsProvider {
            AwsBasicCredentials.create(s3AccessKeyId,s3AccessKeySecret)
        }.endpointOverride(URI.create(s3EndPoint!!)).region(Region.of("auto"))
            .forcePathStyle(s3PathStyle?.toBooleanStrict()?:false).build()
    }
}

fun Application.myApplicationModule() {
    install(CallLogging) {
        level = Level.INFO
    }
    install(Resources)
    install(ContentNegotiation) {
        json()

    }
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        anyHost()
    }
    install(Compression) {
        gzip()
    }
    install(Koin){
        slf4jLogger()
        modules(aooModule)
    }
    routing {
        routeApi()
        singlePageApplication {
            useResources = true
            react("/")
        }
    }
}

