package com.example.generated

import kotlin.Int
import kotlin.String
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Tournament : Table("tournament") {
  val id: Column<Int> = integer("id")

  val name: Column<String> = varchar("name", 2147483647).uniqueIndex("tournament_name_key")

  override val primaryKey: PrimaryKey = PrimaryKey(id)
}
