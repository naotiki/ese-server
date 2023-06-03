import components.component
import csstype.Properties
import emotion.styled.styled
import js.core.jso
import mui.icons.material.GitHub
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Breakpoint
import mui.system.Theme
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.dom.aria.*
import react.dom.events.MouseEvent
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.footer
import react.router.Outlet
import react.router.dom.Link
import react.router.useNavigate
import react.useState
import web.cssom.Color
import web.cssom.FlexGrow
import web.cssom.VerticalAlign
import web.dom.Element
import web.html.HTMLButtonElement
import web.location.location

val Offset = div.styled {
    +it.asDynamic().theme.unsafeCast<mui.material.styles.Theme>().mixins.toolbar.unsafeCast<Properties>()
}
val menuPages = listOf(Pages.Main)
val AppLayout = FC<Props> {

    AppBar {
        //position = AppBarPosition.fixed
        Toolbar {
            // disableGutters=true
            Typography {
                variant = TypographyVariant.h5
                component = div
                sx {

                    flexGrow = 1.unsafeCast<FlexGrow>()
                }
                +"Ese UDON"
            }
            Box {
                menuPages.forEach {
                    Button {
                        component(Link) {
                            to = it.path
                        }
                        color = ButtonColor.inherit
                        +it.pageName
                    }
                }

                Tooltip {
                    title = ReactNode("naotiki/Ese")
                    IconButton {
                        asDynamic().href = "https://github.com/naotiki/Ese"
                        GitHub {
                            sx { verticalAlign = VerticalAlign.middle }
                        }
                    }
                }
                val user = useUser()
                var anchorEl by useState<Element?>(null)
                val navigate = useNavigate()
                if (user is Load.Success) {
                    val open = anchorEl != null
                    val handleClick: (MouseEvent<HTMLButtonElement, web.uievents.MouseEvent>) -> Unit = {
                        anchorEl = it.currentTarget
                    };
                    val handleClose = {
                        anchorEl = null
                    }
                    Button {
                        id = "user-button"
                        ariaControls = ("user-menu".takeIf { open } ?: undefined).toString()
                        ariaHasPopup = AriaHasPopup.`true`
                        ariaExpanded = open
                        onClick = handleClick
                        variant = ButtonVariant.outlined
                        color = ButtonColor.inherit

                        +user.value.userName
                    }
                    Menu {
                        id = "user-menu"

                        this.anchorEl = { anchorEl ?: it }
                        this.open = open
                        onClose = handleClose
                        MenuListProps = jso {
                            ariaLabelledBy = "user-button"
                        }
                        MenuItem {
                            onClick = {
                                navigate("/${user.value.userName}")
                                handleClose()
                            }
                            +"プロフィール"
                        }
                        MenuItem {
                            onClick = {
                                location.href = "/api/logout?redirectUrl="+ location.href
                                handleClose()
                            }
                            +"ログアウト"
                        }
                    }
                } else {

                    Button {
                        variant = ButtonVariant.contained
                        color = ButtonColor.inherit
                        sx {
                            color = Color("black")
                        }
                        href = "/api/login?redirectUrl=" + location.href
                        +"Login"
                    }
                }
            }
        }


    }
    Offset()
    ReactHTML.main {
        //ページが挿入される
        Outlet()
    }
    footer {

    }

}
