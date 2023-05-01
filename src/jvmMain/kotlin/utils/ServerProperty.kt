package utils

import api.JWTFactory
import java.io.File
import java.util.Properties
import kotlin.reflect.KProperty

class ServerProperty() {
    private val propertiesFile = File("local.properties").takeIf { it.exists() }
        ?: File("production.properties").takeIf { it.exists() }
        ?: throw error("Properties File Not Found")

    private val properties=Properties()
    init {
        println("[INFO] Load ${propertiesFile.absolutePath}")
        properties.load(propertiesFile.bufferedReader())
    }

    operator fun getValue(any: Any?, property: KProperty<*>): String {
        return properties.getProperty(property.name)
    }

}