package com.example.generated

import kotlin.Int
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object TournamentPlayer : Table("tournament_player") {
  val tournamentId: Column<Int?> =
      integer("tournament_id").references(Tournament.id).nullable()

  val playerId: Column<Int?> = integer("player_id").references(Player.id).nullable()

  val cumulatedPoint: Column<Int?> = integer("cumulated_point").nullable()
}
