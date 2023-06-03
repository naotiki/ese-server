package utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.jar.JarInputStream

class NoodleValidator {
    suspend fun validate(byteArray: ByteArray):Boolean= withContext(Dispatchers.IO){
        JarInputStream(byteArray.inputStream()).manifest
        true
    }
}