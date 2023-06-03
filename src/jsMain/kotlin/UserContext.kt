import data.PartialUser
import react.*
import web.location.location



private val UserContext = createContext<Load<PartialUser>>(Load.Loading())
fun useUser() = useContext(UserContext)
val UserProvider = FC<PropsWithChildren>("UserProvider") {

    val user = useFetch<PartialUser>("/api/user")
    UserContext.Provider {
        value = user
        child(it.children)
    }

}
external interface RequireUserProps:Props{
    var user:PartialUser
}
external interface RUProps:Props{
    var element:ElementType<RequireUserProps>?
    var onFailed:(()->Unit)?

    var failedElement:ElementType<Props>?
}
val RequireUserWrapper=FC<RUProps>{
    val user = useUser()

    useEffect(user) {
        if (user is Load.Failed) {
            it.onFailed?.invoke()
            console.error("User not found.")
        }
    }
    if (user is Load.Success) {
        it.element?.invoke {
            this.user=user.value
        }

    }else it.failedElement?.invoke()
}
val Dummy=FC<Props>{}

@Deprecated("Hooks違反")
fun RequireUserFC(
    onFailed: () -> Unit = {
        location.href = "/api/login?redirectUrl=" + location.href
    },
    block: ChildrenBuilder.(PartialUser) -> Unit
) = RequireUserFC<Props>(onFailed) { partialUser, _ ->  block(partialUser)}
@Deprecated("Hooks違反")
 fun <P : Props> RequireUserFC(
    onFailed: () -> Unit = {
        location.href = "/api/login?redirectUrl=" + location.href
    },
    block: ChildrenBuilder.(PartialUser, P) -> Unit
) = FC<P> {
    val user = useUser()

    useEffect(user) {
        if (user is Load.Failed) {
            onFailed()
        }
    }
    if (user is Load.Success) {
        block(user.value, it)
    }else Dummy()
}

