import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import mui.material.Container
import mui.material.CssBaseline
import mui.material.Typography
import mui.material.TypographyAlign
import mui.material.styles.ThemeProvider
import mui.material.styles.TypographyVariant
import mui.material.styles.createTheme
import pages.MainPage
import pages.NoodleRepoPage
import pages.UserProfilePage
import react.*
import react.router.*
import react.router.dom.BrowserRouter
import web.location.location

val client = HttpClient(Js) {
    install(ContentNegotiation) {
        json()
    }
}

external interface WaitAPIProps<T : Any> : Props {
    var result: T
}

inline fun <reified T : Any> WaitAPIFetch(element: ElementType<WaitAPIProps<T>>, crossinline urlBuilder: () -> String) =
    FC<Props> {
        val url = urlBuilder()
        val f = useFetch<T>(url, url)
        if (f is Load.Success) {
            element.invoke {
                result = f.value
            }
        }else if( f is Load.Failed){
            child(NotFoundPage)
        }
    }

private fun RequireUserPage(element: ElementType<RequireUserProps>, onFailed: (() -> Unit)? = null) = FC<Props> {
    RequireUserWrapper {
        this.element = element
        this.onFailed = onFailed
    }
}

private fun RequireUserPage(element: ElementType<RequireUserProps>, failedElement: ElementType<Props>) = FC<Props> {
    RequireUserWrapper {
        this.element = element
        this.failedElement = failedElement
    }
}

enum class Pages(
    val page: ElementType<Props>, val path: String, private val routeName: String? = null
) {
    Main(MainPage, "/", "Home"),
    Profile(
        WaitAPIFetch(UserProfilePage) {
            val params = useParams()
            "/api/users/" + params["user"]
        },
        "/:user",
    ),
    NoodleRepo(
        WaitAPIFetch(NoodleRepoPage) {
            val params = useParams()
            "/api/users/"+params["user"]+"/" + params["noodle"]
        },
        "/:user/:noodle",
    ),
    ;

    val pageName get() = routeName ?: name
}

val App = FC<Props> {
    val theme = useMemo(Unit) { createTheme() }
    UserProvider {
        ThemeProvider {
            this.theme = theme
            CssBaseline()

            BrowserRouter {
                Routes {

                    LayoutRoute {
                        element = AppLayout.create()
                        Pages.values().forEach {
                            if (it.path == "/") {
                                react.router.IndexRoute {
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
                            element = NotFoundPage
                        }

                    }
                }
            }
        }
    }
}


val NotFoundPage=Container.create {
    Typography {
        variant = TypographyVariant.h2
        align = TypographyAlign.center
        +"404 Not Found"
    }
    Typography {
        variant = TypographyVariant.h6
        align = TypographyAlign.center
        +"ページが見つかりませんでした"
    }
}
