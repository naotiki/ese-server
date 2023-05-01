package api

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTCreator
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import utils.ServerProperty
import java.util.*

object JWTFactory:KoinComponent {
    val jwtSecret:String by inject<ServerProperty>().value
    val jwtIssuer:String by inject<ServerProperty>().value
    val jwtAudience:String by inject<ServerProperty>().value
    val jwtRealm:String by inject<ServerProperty>().value
    fun createMyJWT(builder:JWTCreator.Builder.()->Unit): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .apply { builder() }
            .withExpiresAt(Date(System.currentTimeMillis()+3600000))
            .sign(Algorithm.HMAC256(jwtSecret))
    }
    fun verifier(): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(jwtSecret))
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .build()
    }
}