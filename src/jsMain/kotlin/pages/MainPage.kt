package pages

import client
import components.NdlUploadButton
import coroutineScope
import io.ktor.client.request.forms.*
import io.ktor.http.*
import js.core.asList
import js.typedarrays.Int8Array
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import mui.icons.material.GitHub
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.*
import web.cssom.AlignItems
import web.cssom.None
import web.cssom.TextAlign
import web.file.File
import web.file.FileList

val MainPage = FC<Props> {
    Container {

        var noodleFile by useState<File?>(null)
        val changedFile = useCallback<(String, FileList?) -> Unit>(noodleFile) { name, files ->
            noodleFile = files?.asList()?.single()
        }

        Stack {
            spacing= responsive(2)
            sx {
                alignItems = AlignItems.center

            }
            Typography {
                variant = TypographyVariant.h3
                sx {
                    textAlign = TextAlign.center
                }
                +"Ese Udon: Downloader Of Noodles"

            }
            Link {
                href = "https://github.com/settings/connections/applications/0ccd2cf5e6bb29b28954"
                +"Review Authentication"
            }
            Button {
                sx {
                    textTransform = None.none
                }
                variant = ButtonVariant.outlined
                size = Size.large
                startIcon = GitHub.create()
                href = "/api/login?redirectUrl=/"
                +"GitHubでログイン"
            }
            NdlUploadButton {
                this.onChangedFile = changedFile
            }
            if (noodleFile != null) {
                +noodleFile?.name

                Button {
                    onClick = {
                        coroutineScope.launch {
                            promise {
                                val byteArray = Int8Array(noodleFile!!.arrayBuffer().await()).unsafeCast<ByteArray>()
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
    }


}