import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import js.core.asList
import js.typedarrays.Int8Array
import kotlinx.coroutines.*
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
import web.cssom.AlignItems
import web.cssom.None
import web.cssom.TextAlign
import web.dom.document
import web.file.File
import web.file.FileList
import web.html.InputType
import web.idb.IDBCursorDirection.Companion.prev
import web.window.Window
import web.window.window

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
    UserProvider{
        ThemeProvider {
            this.theme = theme
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
}



