package com.example.dao

import arrow.core.Either
import com.example.User
import com.example.generated.Users
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

interface UserDao {
    fun find(login: String, password: String): Either<Throwable,User>

}

class UserDaoImpl(): UserDao {
    override fun find(login: String, password: String): Either<Throwable,User> {
        return Either.catch {
            transaction {
                Users.slice(Users.login)
                    .select { (Users.login eq login) and (Users.password eq password) }
                    .map {
                        User(it[Users.login])
                    }.first()
            }
        }
    }

}
