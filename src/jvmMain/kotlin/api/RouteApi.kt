package api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest

@OptIn(KtorExperimentalLocationsAPI::class)
internal fun Routing.routeApi() {
    val s3 by inject<S3Client>()
    route("/api") {
        route("/noodles") {
            get<Noodle> {
                val o=s3.getObject(GetObjectRequest.builder().bucket("ese-noodles").key(it.user+"/"+it.name+".ndl").build())
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, it.name+".ndl")
                        .toString()
                )
                call.respondOutputStream(ContentType("application","ndl+zip")) {
                    o.transferTo(this)
                }
            }
            post<Noodle> {

            }
        }
    }
}

@Location("/{user}/{name}")
data class Noodle(val user: String, val name: String)