package com.example

import arrow.core.Either
import com.example.dao.UserDao
import io.ktor.server.auth.*
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


class AuthServiceImpl(private val userDao: UserDao): AuthService {
    override suspend fun authenticate(login: String, password: String, salt: ByteArray): Either<Throwable, UserIdPrincipal> =
        verifyUser(login, password, salt).map {  UserIdPrincipal(it.login)}




    private fun verifyUser(login: String, password: String, salt: ByteArray): Either<Throwable, User> {
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded
        val base64EncodedHash = Base64.getEncoder().encode(hash)
        return userDao.find(login, String(base64EncodedHash))
    }
}


interface AuthService {
    suspend fun authenticate(login: String, password: String, salt: ByteArray): Either<Throwable, UserIdPrincipal>

}
