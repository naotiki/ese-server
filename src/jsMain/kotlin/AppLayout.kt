import csstype.*
import emotion.react.css
import emotion.styled.styled
import mui.icons.material.GitHub
import mui.icons.material.Menu
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.Breakpoint
import mui.system.Theme
import mui.system.responsive
import mui.system.sx
import react.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.footer
import react.router.Outlet
import web.cssom.*

val Offset = div.styled {
    +it.asDynamic().theme.unsafeCast<mui.material.styles.Theme>().mixins.toolbar.unsafeCast<Properties>()
}
val AppLayout = FC<Props> {
div.styled(){

}
    var isDrawerOpen by useState(false)
    val isDownSm = useMediaQuery<Theme>({
        it.breakpoints.down(Breakpoint.sm)
    })
    if (isDownSm) {
        Drawer {
            anchor = DrawerAnchor.left
            open = isDrawerOpen
            onClose = { _, _ ->
                isDrawerOpen = !isDrawerOpen
            }
            Stack {
                direction= responsive(StackDirection.column)
                sx {
                    width = 250.px
                    height=100.pct
                }
                List {
                    ListItem {
                        +"Ese UDON"
                    }
                    Pages.values().forEach {
                        ListItem {
                            Link {
                                href = it.path
                                ListItemButton {
                                    ListItemText {
                                        +it.getName()
                                    }
                                }
                            }
                        }
                    }
                }
                div{
                    css {
                        marginTop= Auto.auto
                    }
                }
                Button {
                    href = "https://github.com/naotiki/Ese"
                    GitHub()
                    +"naotiki/Ese"
                }
            }
        }
        Fab {
            sx {
                position = Position.absolute
                top = 16.px
                left = 16.px
            }
            onClick = {
                isDrawerOpen = true
            }
            Menu()
        }
    } else {
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
                    Pages.values().forEach {
                        Button {
                            color = ButtonColor.inherit
                            href = it.path
                            +it.getName()
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