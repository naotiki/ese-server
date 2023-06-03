import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import react.useEffect
import react.useState

sealed interface Load<T : Any> {
    class Loading<T : Any> : Load<T>
    data class Success<T : Any>(val value: T) : Load<T>
    class Failed<T : Any> : Load<T>

    fun getOrNull(): T? = (this as? Success)?.value
}

/**
 * @param T 応答をこの型にキャストします。
 * @param url 取得するURL
 * @param dependencies [url]がStateに影響される場合などはここにいれる
 */
inline fun <reified T : Any> useFetch(url: String, vararg dependencies: Any? = emptyArray()): Load<T> {
    var loadState by useState<Load<T>>(Load.Loading())
    useEffect(*dependencies) {
        coroutineScope.launch {
            loadState = runCatching { client.get(url).body<T>() }.fold(
                {
                    Load.Success(it)
                }, {
                    Load.Failed()
                }
            )
        }
    }
    return loadState
}

inline fun <reified T : Any> useFetchOrNull(url: String, vararg dependencies: Any? = emptyArray()): T? {
    var loadState by useState<T?>(null)
    useEffect(*dependencies) {
        coroutineScope.launch {
            loadState = runCatching { client.get(url).body<T>() }.getOrNull()
        }
    }
    return loadState
}
