package com.example

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.dao.UserDao


class UserDaoTestImpl: UserDao {
    override fun find(login: String, password: String): Either<Throwable,User> {
        return if (login == "admin" ) User(login).right()
        else IllegalAccessException("unAuthorized").left()
    }

}
