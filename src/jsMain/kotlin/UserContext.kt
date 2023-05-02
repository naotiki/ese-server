import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import mui.material.MuiTouchRipple.Companion.child
import react.*
import web.cssom.HtmlAttributes.Companion.value

private val UserContext = createContext<PartialUser?>(null)
fun useUser()= useContext(UserContext)
val UserProvider= FC<PropsWithChildren>("UserProvider") {
    var user by useState<PartialUser?>(null)
    useEffectOnce {
        coroutineScope.launch {
            val partialUser= runCatching{ client.get("/api/user").body<PartialUser>() }.getOrNull()
            user=partialUser
        }
    }
    UserContext.Provider{
        value=user
        child(it.children)
    }

}