package com.example

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.flatMap
import com.example.dao.PlayerDao
import com.example.dao.ScoreDao
import com.example.dao.TournamentDao


interface ScoreService {
    suspend fun registerScore(tournamentId: Int, playerId: Int, amount: Int): Either<Throwable, Score>
    suspend fun get(tournamentId: Int, playerId: Int):  Either<Throwable, Score>
    suspend fun getBoardScore(tournamentId: Int): Either<Throwable, List<Score>>
}

class ScoreServiceImpl(val tournamentDao: TournamentDao, val playerDao: PlayerDao, val scoreDao: ScoreDao): ScoreService {

    override suspend fun registerScore(tournamentId: Int, playerId: Int, amount: Int): Either<Throwable, Score> {
        return either {
            val tournament = tournamentDao.fetch(tournamentId).bind()
            val player = playerDao.fetch(playerId).bind()
            scoreDao.register(tournament.id, player.id, amount).bind()
            get(tournamentId, playerId).bind()
        }
    }

    override suspend fun get(tournamentId: Int, playerId: Int): Either<Throwable, Score> {
        return getBoardScore(tournamentId).flatMap { scoreList ->
            Either.catch {
                scoreList.first { it.playerId == playerId }
            }
        }
    }

    override suspend fun getBoardScore(tournamentId: Int): Either<Throwable, List<Score>> {
        return either {
            val boardScore: MutableList<Score> = mutableListOf()
            val scores = scoreDao.getScoreboard(tournamentId).bind()
            val groupedScores = scores.groupBy { it.amount }.toSortedMap()
            val points = groupedScores.keys.reversed()
            points.mapIndexed { order, score ->
                groupedScores[score]?.forEach { boardScore.add(it.copy(rank = order+1)) }
            }
            boardScore
        }
    }

}
