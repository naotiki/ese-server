package api.s3repo

import io.ktor.resources.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectResponse

private const val eseNoodleBucket = "ese-noodles"

class NoodleS3Repository : KoinComponent {
    private val s3Client by inject<S3Client>()

    fun getNoodleBytesOrNull(noodle: Noodle): ResponseInputStream<GetObjectResponse>? {
        return s3Client.runCatching {
            getObject {
                it.bucket(eseNoodleBucket)
                it.key(
                    if (noodle.version == null)
                        noodle.latestPath
                    else
                        noodle.fullPath
                )
            }
        }.getOrNull()
    }

    fun putNoodleBytes(byteArray: ByteArray, noodle: Noodle): Result<PutObjectResponse> {

        check(noodle.version != null)
        return runCatching {
            s3Client.putObject(
                {
                    it.bucket(eseNoodleBucket)
                    it.key(noodle.fullPath)
                    it.contentType("application/ndl+zip")
                },
                RequestBody.fromBytes(byteArray)
            )
        }.onSuccess {
            s3Client.copyObject {
                it.sourceKey(noodle.fullPath)
                it.destinationKey(noodle.latestPath)
                it.sourceBucket(eseNoodleBucket)
                it.destinationBucket(eseNoodleBucket)
                it.contentType("application/ndl+zip")
            }
        }

    }
}

@Resource("/{user}/{name}")
 class Noodle(val user: String, val name: String, val version: String? = null) {
    private val path get() = "$user/$name"
    val fileName get() = "$user-$name-$version.ndl"
    val latestFileName get() = "$user-$name.ndl"
    val fullPath get() = "$path/$fileName"

    val latestPath get() = "$path/$latestFileName"
}