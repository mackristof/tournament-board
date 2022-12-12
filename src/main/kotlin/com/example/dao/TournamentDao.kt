package com.example.dao

import arrow.core.Either
import arrow.core.flatMap
import com.example.Tournament
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

interface TournamentDao {
    fun list(): Either<Throwable,List<Tournament>>
    fun save(tournament: Tournament): Either<Throwable,Tournament>

    fun fetch(id: Int): Either<Throwable, Tournament>
    fun delete(tournamentId: Int): Either<Throwable, Unit>
}

class TournamentDaoImpl(): TournamentDao {
    override fun list(): Either<Throwable,List<Tournament>>{
        return Either.catch {
            transaction {
                com.example.generated.Tournament.slice(
                    com.example.generated.Tournament.id,
                    com.example.generated.Tournament.name)
                    .selectAll()
                    .map {
                        Tournament(id = it[com.example.generated.Tournament.id],
                            name = it[com.example.generated.Tournament.name])
                    }
            }
        }
    }

    override fun fetch(id:Int): Either<Throwable, Tournament>{
        return transaction {
            Either.catch {
                com.example.generated.Tournament.select { com.example.generated.Tournament.id eq id }.map {
                    Tournament(
                        id = it[com.example.generated.Tournament.id],
                        name = it[com.example.generated.Tournament.name]
                    )
                }.first()
            }
        }
    }

    override fun delete(tournamentId: Int): Either<Throwable, Unit> {
        return transaction {
            Either.catch {
                com.example.generated.TournamentPlayer.deleteWhere { com.example.generated.TournamentPlayer.tournamentId eq tournamentId }
                val count = com.example.generated.Tournament.deleteWhere { id eq tournamentId }
            }

        }
    }

    override fun save(tournament: Tournament): Either<Throwable, Tournament> {

            return transaction {
                fetch(tournament.id).fold(
                    ifLeft = {
                        Either.catch {
                            com.example.generated.Tournament.insert {
                                it[id] = tournament.id
                                it[name] = tournament.name
                            }.resultedValues!!.map {
                                Tournament(
                                    id = it[com.example.generated.Tournament.id],
                                    name = it[com.example.generated.Tournament.name]
                                )
                            }.first()
                        }
                    },
                    ifRight = {
                        Either.catch {
                            com.example.generated.Tournament.update({
                                com.example.generated.Tournament.id.eq(tournament.id)
                            }) {
                                it[name] = tournament.name
                            }
                        }.flatMap {  fetch(tournament.id) }

                    }
                )

            }
    }

}
