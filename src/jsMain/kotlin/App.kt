import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import js.core.asList
import js.typedarrays.Int8Array
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import mui.material.Button
import mui.material.ButtonVariant
import react.FC
import react.Props
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.input
import react.useCallback
import react.useState
import web.file.File
import web.file.FileList
import web.html.InputType
import web.location.location

val client = HttpClient(Js) {
    install(ContentNegotiation) {
    }
}
val App = FC<Props> {
    var noodleFile by useState<File?>(null)
    val changedFile = useCallback<(String, FileList?) -> Unit>(noodleFile) { name, files ->
        noodleFile = files?.asList()?.single()
    }
    NdlUploadButton {
        this.onChangedFile = changedFile
    }
    br()
    if (noodleFile != null) {
        +noodleFile?.name

        Button {
            onClick = {
                GlobalScope.launch {
                    promise {
                        val byteArray=Int8Array(noodleFile!!.arrayBuffer().await()).unsafeCast<ByteArray>()
                        client.submitFormWithBinaryData(
                            url = "/api/noodles/testUserJS/${
                                noodleFile!!.name.replaceAfterLast(
                                    ".",
                                    ""
                                ).trim('.')
                            }?version=0.0.1", formData {

                                noodleFile!!.stream().getReader()

                                append("ndl", byteArray, Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=\"${noodleFile!!.name}\"")
                                })
                            })
                    }.await()

                }
            }
            +"Upload"
        }


    }


}

external interface NdlUploadButtonProps : Props {
    var onChangedFile: (String, FileList?) -> Unit
}

val NdlUploadButton = FC<NdlUploadButtonProps> {
    Button {
        variant = ButtonVariant.contained
        +"Select NDL File"
        asDynamic().component = "label"
        input {
            hidden = true
            accept = ".ndl,application/ndl+zip"
            type = InputType.file
            onChange = { event ->
                it.onChangedFile(event.target.name, event.target.files)
            }
        }
    }
}