package components

import coroutineScope
import emotion.react.css
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mui.icons.material.ContentCopy
import mui.material.Box
import mui.material.IconButton
import mui.material.IconButtonColor
import mui.material.Tooltip
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.ReactHTML.span
import react.useState
import web.cssom.FontFamily
import web.cssom.px
import web.cssom.rgb
import web.navigator.navigator

val CommandBox= FC<CommandBoxProps> {
    Box {
        sx {
            padding = 10.px
            borderRadius=10.px
            backgroundColor = rgb(10, 10, 10)
        }
        component = span
        span {
            css {
                color = rgb(255, 255, 255)

                fontFamily="'JetBrains Mono',monospace".unsafeCast<FontFamily>()
            }
            +it.commandString
        }
        var tooltipText by useState("Copy")
        Tooltip{
            title= ReactNode(tooltipText)
            arrow=true
            IconButton{
                color= IconButtonColor.primary
                onClick={_->
                    coroutineScope.launch {
                        navigator.clipboard.writeText(it.commandString).await()
                        tooltipText="Copied!!!"
                        delay(1000)
                        tooltipText="Copy"
                    }
                }
                ContentCopy()
            }
        }
    }
}

external interface CommandBoxProps : Props {
    var commandString:String
}