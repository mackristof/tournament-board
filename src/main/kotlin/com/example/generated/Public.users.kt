package com.example.generated

import kotlin.String
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column

object Users : IntIdTable("users", "id") {
  val login: Column<String> = varchar("login", 2147483647).uniqueIndex("users_login_key")

  val password: Column<String> = varchar("password", 2147483647)
}
