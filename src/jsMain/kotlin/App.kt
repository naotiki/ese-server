import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import js.core.asList
import js.typedarrays.Int8Array
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import mui.icons.material.GitHub
import mui.material.*
import mui.material.styles.ThemeProvider
import mui.material.styles.TypographyVariant
import mui.material.styles.createTheme
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.p
import react.router.IndexRoute
import react.router.LayoutRoute
import react.router.PathRoute
import react.router.Routes
import react.router.dom.BrowserRouter
import web.cssom.None
import web.dom.document
import web.file.File
import web.file.FileList
import web.html.InputType

val client = HttpClient(Js) {
    install(ContentNegotiation) {
        json()
    }
}

enum class Pages(
    val page: ElementType<Props>, val path: String, private val routeName: String? = null
) {
    Main(MainPage, "/");
    // Works(WorksPage, "/works"),
    // Assets(AssetsPage, "/assets");

    fun getName() = routeName ?: name
}

val App = FC<Props> {
    val theme = useMemo(Unit){ createTheme()}
    ThemeProvider {
        this.theme=theme
        CssBaseline()
        BrowserRouter {
            Routes {
                LayoutRoute {
                    element = AppLayout.create()
                    Pages.values().forEach {
                        if (it.path == "/") {
                            IndexRoute {
                                index = true
                                element = it.page.create()
                            }
                        } else {
                            PathRoute {
                                path = it.path
                                element = it.page.create()
                            }
                        }

                    }
                    PathRoute {
                        path = "*"
                        element =
                            Container.create {
                                Typography {
                                    variant = TypographyVariant.h1
                                    align = TypographyAlign.center
                                    +"404 Not Found"
                                }
                                Typography {
                                    variant = TypographyVariant.h6
                                    align = TypographyAlign.center
                                    +"ページが見つかりませんでした"
                                }
                            }
                    }
                }
            }
        }
    }
}

val MainPage = FC<Props> {
    var noodleFile by useState<File?>(null)
    val changedFile = useCallback<(String, FileList?) -> Unit>(noodleFile) { name, files ->
        noodleFile = files?.asList()?.single()
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
    p {
        +"aaaaa"
        +kotlinx.browser.document.cookie
        +document.cookie
        +parseClientCookiesHeader(document.cookie).toList().joinToString()
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