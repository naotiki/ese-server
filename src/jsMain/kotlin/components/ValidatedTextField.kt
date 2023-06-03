package components

import components.ValidatedTextFieldValue.*
import mui.material.TextField
import mui.material.TextFieldProps
import org.w3c.dom.HTMLInputElement
import pages.redText
import react.*
import react.dom.onChange
import web.cssom.HtmlAttributes.Companion.value

val ValidatedTextField = FC<ValidatedTextFieldProps> {
    useEffectOnce {
        it.onChange?.invoke(
            it.validator?.invoke(
                it.value.value
            ) ?: Valid(it.value.value)
        )
    }
    TextField {
        value = it.value.value
        it.textFieldProps(this)
        onChange = { e ->
            it.onChange?.invoke(
                it.validator?.invoke(
                    e.currentTarget.unsafeCast<HTMLInputElement>().value
                ) ?: Valid(e.currentTarget.unsafeCast<HTMLInputElement>().value)
            )
        }
        if ((it.value as? Invalid)?.helperText != null)
            helperText = redText.create { +(it.value as Invalid).helperText }
    }
}

 sealed class ValidatedTextFieldValue(val value: String) {
     class Valid(value: String) : ValidatedTextFieldValue(value)
     class Invalid( value: String, val helperText: String? = null) : ValidatedTextFieldValue(value)
}

external interface ValidatedTextFieldProps : Props {
    var validator: ((String) -> ValidatedTextFieldValue)?
    var textFieldProps: (TextFieldProps) -> Unit
    var value: ValidatedTextFieldValue
    var onChange: ((ValidatedTextFieldValue) -> Unit)?
}
