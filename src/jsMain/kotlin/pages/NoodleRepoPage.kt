package pages

import WaitAPIProps
import components.CommandBox
import data.NoodleData
import data.NoodleRepositoryData
import emotion.react.css
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p
import react.dom.html.TdAlign
import react.router.useNavigate
import useFetchOrNull
import useUser
import web.cssom.*
import kotlin.js.Date

val NoodleRepoPage = FC<WaitAPIProps<NoodleRepositoryData>> {
    Container {
        val user=useUser()
        maxWidth = "lg"
        Stack {
            sx {
                alignItems = AlignItems.baseline
            }
            spacing = responsive(2)
            direction = responsive(StackDirection.row)
            Typography {
                sx { color = rgb(200, 200, 200) }
                variant = TypographyVariant.h4
                +it.result.user.userName
            }
            Typography {
                sx { color = rgb(200, 200, 200) }
                variant = TypographyVariant.h4
                +"/"
            }
            Typography {
                variant = TypographyVariant.h2
                +it.result.name
            }
        }
        CommandBox {
            commandString = "udon world " + it.result.user.userName + "/" + it.result.name
        }
        p {
            +(it.result.description ?: "(説明文なし)")
        }
        it.result.url?.let {
            div {
                css {
                    display = Display.flex
                }
                mui.icons.material.Link {
                    sx {
                        marginRight = 5.px
                    }
                }

                Link {
                    href = it
                    +it
                }
            }
        }
        val nav= useNavigate()
        val noodles=useFetchOrNull<List<NoodleData>>("/api/users/${it.result.user.userName}/${it.result.name}/all")
        TableContainer {
            component = Paper
            Table {
                TableHead {
                    TableRow {
                        TableCell {
                            +"バージョン名"
                        }
                        TableCell {
                            align = TdAlign.right
                            +"作成日"
                        }
                    }
                }
                TableBody{
                    user.getOrNull()?.let {user->
                        if (user==it.result.user){
                            TableRow {
                                TableCell {
                                    colSpan = 2
                                    Button {
                                        sx { width = 100.pct }
                                        variant = ButtonVariant.contained
                                        onClick = {

                                            // dialogOpen = true
                                        }
                                        +"新しいバージョンを公開"
                                    }
                                }
                            }
                        }
                    }
                    noodles?.forEach {noodle->
                        TableRow {
                            hover = true
                            onClick = { _ ->
                            }
                            sx {
                                cursor= Cursor.pointer
                            }
                            TableCell {
                                +noodle.version
                            }
                            TableCell {
                                align = TdAlign.right
                                +Date(noodle.createdAt).toLocaleString()
                            }

                        }
                    }

                }
            }
        }

    }
}


