import dao.DatabaseFactory
import dao.UserDAOFacadeImpl
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test
import utils.UIDGenerator
import kotlin.test.assertTrue
import kotlin.time.Duration


class UnitTest {
    @Test
    fun DBTest() = runTest(timeout = Duration.INFINITE) {

        DatabaseFactory.init()
        val u=UserDAOFacadeImpl()

        runBlocking {
            repeat(1_000){
                launch {
                    repeat(10){
                        u.addUser(UIDGenerator.nextUID(),"","","")
                    }
                }
            }
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun test() = runTest(timeout = Duration.INFINITE) {
        val set = mutableSetOf<Long>()
        runBlocking {
            repeat(100000) {

                launch {
                    repeat(10) {
                        val a = UIDGenerator.nextUID()
                        assertTrue(set.add(a), "Duplicated ${a.toString(2)} in $it times")
                    }
                }


            }
        }
        println(set.toList().takeLast(20).joinToString("\n"){
            UIDGenerator.stringFromUID(it)
        })
        println(set.size)


    }
}