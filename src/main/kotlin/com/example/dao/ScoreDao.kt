package com.example.dao

import arrow.core.Either
import com.example.Score
import com.example.generated.TournamentPlayer
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

interface ScoreDao {
    fun register(tournamentId: Int, playerId: Int, amount: Int): Either<Throwable, Score>
    fun fetch(tournamentId: Int, playerId: Int): Either<Throwable, Score>
    fun getScoreboard(tournamentId: Int): Either<Throwable, List<Score>>
}

class ScoreDaoImpl(): ScoreDao {
    override fun register(tournamentId: Int, playerId: Int, amount: Int): Either<Throwable, Score> {
        return transaction {
                fetch(tournamentId, playerId).fold(
                    ifLeft = {
                        Either.catch{
                            TournamentPlayer.insert {
                                it[TournamentPlayer.tournamentId] = tournamentId
                                it[TournamentPlayer.playerId] = playerId
                                it[cumulatedPoint] = amount
                            }.resultedValues!!.map {
                                com.example.Score(
                                    tournamentId = it[TournamentPlayer.tournamentId]!!,
                                    playerId = it[TournamentPlayer.playerId]!!,
                                    amount = it[TournamentPlayer.cumulatedPoint]!!,
                                    rank = 0
                                )
                            }.first()
                        }
                    },
                    ifRight = {oldScore ->
                        Either.catch{
                            TournamentPlayer.update( {
                                TournamentPlayer.tournamentId.eq(tournamentId)
                                    .and(TournamentPlayer.playerId.eq(playerId))})
                            {
                                it[cumulatedPoint] = amount+oldScore.amount
                            }
                            oldScore.copy(amount= amount+oldScore.amount)
                        }
                    }
                )
            }
        }

    override fun fetch(tournamentId: Int, playerId: Int): Either<Throwable, Score>{
        return transaction {
            Either.catch {
                TournamentPlayer.select {
                    (TournamentPlayer.tournamentId.eq(tournamentId))
                        .and(TournamentPlayer.playerId.eq(playerId))

                }.map {
                    Score(
                        tournamentId = it[TournamentPlayer.tournamentId]!!,
                        playerId = it[TournamentPlayer.playerId]!!,
                        amount = it[TournamentPlayer.cumulatedPoint]!!,
                        rank = 0
                    )
                }.first()
            }
        }
    }

    override fun getScoreboard(tournamentId: Int): Either<Throwable, List<Score>> {
        return transaction {
            Either.catch {
                TournamentPlayer.select {
                    TournamentPlayer.tournamentId.eq(tournamentId)
                }.map {
                    Score(
                        tournamentId = it[TournamentPlayer.tournamentId]!!,
                        playerId = it[TournamentPlayer.playerId]!!,
                        amount = it[TournamentPlayer.cumulatedPoint]!!,
                        rank = 0
                    )
                }
            }
        }
    }


}
