package com.example.generated

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Player : Table("player") {
  val id: Column<Int> = integer("id")

  val account: Column<String> = varchar("account",
      2147483647).uniqueIndex("player_account_key")

  override val primaryKey: PrimaryKey = PrimaryKey(id)
}
