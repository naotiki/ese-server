package api

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.jvm.javaio.*
import org.koin.ktor.ext.inject
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest

const val eseNoodleBucket = "ese-noodles"
internal fun Routing.routeApi() {
    val s3 by inject<S3Client>()
    route("/api") {
        route("/noodles") {
            get<Noodle> {

                val o = s3.runCatching {
                    getObject(
                        GetObjectRequest.builder().bucket(eseNoodleBucket).key(it.fullPath).build()
                    )
                }.getOrNull()
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

                    s3.putObject(
                        PutObjectRequest.builder().bucket(eseNoodleBucket).key(it.fullPath).contentType("application/ndl+zip").build(),
                        RequestBody.fromBytes(file.streamProvider().readBytes())
                    )
                }
                call.respond("OK")

            }
        }
    }
}

@Resource("/{user}/{name}")
data class Noodle(val user: String, val name: String, val version: String? = null) {
    val path get() = "$user/$name"
    val fileName get() = "$user-$name-$version.ndl"
    val fullPath get() = "$path/$fileName"
}