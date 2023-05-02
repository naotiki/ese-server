
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import mui.material.Container
import mui.material.Typography
import mui.material.TypographyAlign
import mui.material.styles.TypographyVariant
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.router.LayoutRoute
import react.router.PathRoute
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter
import web.cssom.HtmlAttributes.Companion.align
import web.cssom.UserSelect.Companion.element
import web.dom.document
val coroutineScope= CoroutineScope( window.window.asCoroutineDispatcher())
fun main() {
    val container = document.createElement("div").also {
        document.body.appendChild(it)
    }
    createRoot(container).render(
        App.create()
    )
}

