package pages


import Load
import RequireUserProps
import WaitAPIProps
import client
import components.ValidatedTextField
import components.ValidatedTextFieldValue
import components.ValidatedTextFieldValue.Invalid
import components.ValidatedTextFieldValue.Valid
import components.unsafeProps
import coroutineScope
import data.*
import emotion.styled.styled
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.span
import react.dom.html.TdAlign
import react.router.dom.Link
import react.router.dom.LinkProps
import react.router.useNavigate
import react.router.useParams
import useFetch
import useFetchOrNull
import useUser
import web.cssom.AlignItems
import web.cssom.Cursor
import web.cssom.pct
import web.cssom.rgb
import kotlin.js.Date

val UserProfilePage=FC<WaitAPIProps<PartialUser>>{
    val params = useParams()
    val isLoginUser=it.result.userDetails!=null
    val nav= useNavigate()

    Container {
        maxWidth="lg"
        Stack {
            sx {
                alignItems = AlignItems.baseline
            }
            direction = responsive(StackDirection.row)
            Typography {
                variant = TypographyVariant.h1
                +it.result.userName
            }
            Typography {
                sx { color = rgb(200, 200, 200) }
                variant = TypographyVariant.caption
                +it.result.id.toString()
            }
        }
        Typography {
            variant = TypographyVariant.h3
            +"Noodles"
        }
        val noodles: List<NoodleRepositoryData>? =
            useFetchOrNull("/api/users/${params["user"]}/noodleRepositories")
        var dialogOpen by useState(false)
        TableContainer {
            component = Paper
            Table {
                TableHead {
                    TableRow {
                        TableCell {
                            +"名前"
                        }
                        TableCell {
                            align = TdAlign.right
                            +"最新バージョン"
                        }
                        TableCell {
                            align = TdAlign.right
                            +"最終更新"
                        }
                    }
                }
                TableBody {
                    if (isLoginUser) {
                        TableRow {
                            TableCell {
                                colSpan = 3
                                Button {
                                    sx { width = 100.pct }
                                    variant = ButtonVariant.contained
                                    onClick = {

                                        dialogOpen = true
                                    }
                                    +"新しいNoodleRepoを追加"
                                }
                            }
                        }
                    }

                        noodles.orEmpty().forEach {
                            TableRow {
                                hover=true
                                onClick={_->
                                    nav("/${it.user.userName}/${it.name}")
                                }
                                sx {
                                    cursor= Cursor.pointer
                                }
                                TableCell {
                                    +it.name
                                }
                                TableCell {
                                    align = TdAlign.right
                                    +(it.latestVersion?.version ?: "なし")
                                }
                                TableCell {
                                    align = TdAlign.right
                                    +Date(it.updatedAt).toLocaleString()
                                }
                            }
                        }

                }
            }
        }

        Dialog {
            open = dialogOpen
            fullWidth = true
            maxWidth = "md"
            scroll = DialogScroll.paper

            onClose = { _, reason /*"escapeKeyDown" or "backdropClick"*/ ->
                if (reason != "backdropClick")
                    dialogOpen = false
            }

            var name: ValidatedTextFieldValue by useState(Valid(""))
            var description: ValidatedTextFieldValue by useState(Valid(""))
            var url: ValidatedTextFieldValue by useState(Valid(""))
            var nameState by useState(Level.OK)
            useEffect(dialogOpen) {
                name = Valid("")
                description = Valid("")
                url = Valid("")
                nameState = Level.OK
            }
            DialogTitle {
                +"新しいNoodleRepoを作成。"
            }
            DialogContent {
                DialogContentText {
                    +"新しいNoodleRepoを作成します。"
                }
                Stack {
                    spacing = responsive(2)
                    ValidatedTextField {
                        textFieldProps = {
                            it.autoFocus = true
                            it.label = redText.create { +"名前 (必須)" }

                            it.helperText = ReactNode("後から変更することはできません。")
                            if (nameState == Level.Warn) {
                                it.helperText = yellowText.create { +"他のユーザーが使用しています。" }
                            }

                        }
                        value = name
                        validator = {
                            if (it.isBlank()) {
                                Invalid(it, "空白は無効です。")
                            } else Valid(it)
                        }
                        var job by useState<Job?>(null)
                        onChange = {
                            name = it
                            job?.cancel()
                            if (it is Valid)
                                job = coroutineScope.launch {
                                    val l = client.get("/api/user/noodleRepositories/checkName?name=${it.value}")
                                        .body<Level>()
                                    nameState = l
                                    if (l == Level.Error)
                                        name = Invalid(it.value, "既に使用されています。")
                                }

                        }
                    }
                    ValidatedTextField {
                        textFieldProps = {
                            it.label = ReactNode("説明")
                            it.multiline = true
                        }
                        value = description
                        onChange = {
                            description = it
                        }
                    }

                    ValidatedTextField {
                        textFieldProps = {
                            it.label = ReactNode("URL")
                            it.placeholder = "https://github.com/naotiki/Ese"
                            it.multiline = true
                        }
                        validator = {

                            if (it.isNotEmpty() &&
                                !urlRegex.matches(it)
                            )
                                Invalid(it, "有効なURLを入力してください")
                            else Valid(it)
                        }
                        value = url
                        onChange = {
                            url = it
                        }
                    }
                }
            }
            DialogActions {
                Button {
                    onClick = {
                        dialogOpen = false
                    }
                    +"キャンセル"
                }
                Button {
                    disabled = !(name is Valid && description is Valid && url is Valid)
                    onClick = {
                        coroutineScope.launch {
                            client.put("/api/user/noodleRepositories") {
                                contentType(ContentType.Application.Json)
                                this.setBody(
                                    CreateNoodleRepositoryData(
                                        name.value, description.value, url.value
                                    )
                                )
                            }
                        }
                    }
                    +"作成"
                }
            }
        }

    }
}
val UserProfilePageR = FC<RequireUserProps> {
    Container {
        Stack {
            sx {
                alignItems = AlignItems.baseline
            }
            direction = responsive(StackDirection.row)
            Typography {
                variant = TypographyVariant.h1
                +it.user.userName
            }
            Typography {
                sx { color = rgb(200, 200, 200) }
                variant = TypographyVariant.caption
                +it.user.id.toString()
            }
        }
        Typography {
            variant = TypographyVariant.h3
            +"Noodles"
        }
        val noodles: Load<List<NoodleRepositoryData>> =
            useFetch("/api/user/noodleRepositories")
        var dialogOpen by useState(false)
        TableContainer {
            component = Paper
            Table {
                TableHead {
                    TableRow {
                        TableCell {
                            +"名前"
                        }
                        TableCell {
                            align = TdAlign.right
                            +"最新バージョン"
                        }
                        TableCell {
                            align = TdAlign.right
                            +"最終更新"
                        }
                    }
                }
                TableBody {
                    TableRow {
                        TableCell {
                            colSpan = 3
                            Button {
                                sx { width = 100.pct }
                                variant = ButtonVariant.contained
                                onClick = {

                                    dialogOpen = true
                                }
                                +"新しいNoodleRepoを追加"
                            }
                        }
                    }
                    if (noodles is Load.Success) {
                        noodles.value.forEach {
                            TableRow {
                                TableCell {
                                    component = Link
                                    unsafeProps<LinkProps> {}
                                    +it.name
                                }
                                TableCell {
                                    align = TdAlign.right
                                    +(it.latestVersion?.version ?: "なし")
                                }
                                TableCell {
                                    align = TdAlign.right
                                    +Date(it.updatedAt).toLocaleString()
                                }
                            }
                        }
                    }
                }
            }
        }

        Dialog {
            open = dialogOpen
            fullWidth = true
            maxWidth = "md"
            scroll = DialogScroll.paper

            onClose = { _, reason /*"escapeKeyDown" or "backdropClick"*/ ->
                if (reason != "backdropClick")
                    dialogOpen = false
            }

            var name: ValidatedTextFieldValue by useState(Valid(""))
            var description: ValidatedTextFieldValue by useState(Valid(""))
            var url: ValidatedTextFieldValue by useState(Valid(""))
            var nameState by useState(Level.OK)
            useEffect(dialogOpen) {
                name = Valid("")
                description = Valid("")
                url = Valid("")
                nameState = Level.OK
            }
            DialogTitle {
                +"新しいNoodleRepoを作成。"
            }
            DialogContent {
                DialogContentText {
                    +"新しいNoodleRepoを作成します。"
                }
                Stack {
                    spacing = responsive(2)
                    ValidatedTextField {
                        textFieldProps = {
                            it.autoFocus = true
                            it.label = redText.create { +"名前 (必須)" }

                            it.helperText = ReactNode("後から変更することはできません。")
                            if (nameState == Level.Warn) {
                                it.helperText = yellowText.create { +"他のユーザーが使用しています。" }
                            }

                        }
                        value = name
                        validator = {
                            if (it.isBlank()) {
                                Invalid(it, "空白は無効です。")
                            } else Valid(it)
                        }
                        var job by useState<Job?>(null)
                        onChange = {
                            name = it
                            job?.cancel()
                            if (it is Valid)
                                job = coroutineScope.launch {
                                    val l = client.get("/api/user/noodleRepositories/checkName?name=${it.value}")
                                        .body<Level>()
                                    nameState = l
                                    if (l == Level.Error)
                                        name = Invalid(it.value, "既に使用されています。")
                                }

                        }
                    }
                    ValidatedTextField {
                        textFieldProps = {
                            it.label = ReactNode("説明")
                            it.multiline = true
                        }
                        value = description
                        onChange = {
                            description = it
                        }
                    }

                    ValidatedTextField {
                        textFieldProps = {
                            it.label = ReactNode("URL")
                            it.placeholder = "https://github.com/naotiki/Ese"
                            it.multiline = true
                        }
                        validator = {

                            if (it.isNotEmpty() &&
                                !urlRegex.matches(it)
                            )
                                Invalid(it, "有効なURLを入力してください")
                            else Valid(it)
                        }
                        value = url
                        onChange = {
                            url = it
                        }
                    }
                }
            }
            DialogActions {
                Button {
                    onClick = {
                        dialogOpen = false
                    }
                    +"キャンセル"
                }
                Button {
                    disabled = !(name is Valid && description is Valid && url is Valid)
                    onClick = {
                        coroutineScope.launch {
                            client.put("/api/user/noodleRepositories") {
                                contentType(ContentType.Application.Json)
                                this.setBody(
                                    CreateNoodleRepositoryData(
                                        name.value, description.value, url.value
                                    )
                                )
                            }
                        }
                    }
                    +"作成"
                }
            }
        }

    }
}

val redText = span.styled {
    color = rgb(255, 0, 0)
}
val yellowText = span.styled {
    color = rgb(121, 121, 0)
}