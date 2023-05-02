package components

import mui.material.Button
import mui.material.ButtonVariant
import react.FC
import react.Props
import react.dom.html.ReactHTML.input
import web.file.FileList
import web.html.InputType

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