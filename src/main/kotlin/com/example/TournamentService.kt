package com.example

import arrow.core.Either
import com.example.dao.TournamentDao

interface TournamentService {
    fun list(): Either<Throwable, List<Tournament>>
    fun add(tournament: Tournament): Either<Throwable, Tournament>
    fun delete(tournamentId: Int): Either<Throwable, Unit>
}


class TournamentServiceImpl(val tournamentDao: TournamentDao): TournamentService {
    override fun list(): Either<Throwable, List<Tournament>> {
        return tournamentDao.list()
    }

    override fun add(tournament: Tournament): Either<Throwable, Tournament> {
        return tournamentDao.save(tournament)
    }

    override fun delete(tournamentId: Int): Either<Throwable, Unit> {
        return tournamentDao.delete(tournamentId)
    }

}
