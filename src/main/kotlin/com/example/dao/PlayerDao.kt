package com.example.dao

import arrow.core.Either
import arrow.core.flatMap
import com.example.Player
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

interface PlayerDao {
    fun list(): Either<Throwable,List<Player>>
    fun save(player: Player): Either<Throwable,Player>

    fun fetch(id: Int): Either<Throwable, Player>
}

class PlayerDaoImpl: PlayerDao {
    override fun list(): Either<Throwable,List<Player>>{
        return Either.catch {
            transaction {
                com.example.generated.Player.slice(
                    com.example.generated.Player.id,
                    com.example.generated.Player.account)
                    .selectAll()
                    .map {
                        Player(id = it[com.example.generated.Player.id],
                            name = it[com.example.generated.Player.account])
                    }
            }
        }
    }

    override fun fetch(id:Int): Either<Throwable, Player>{
        return transaction {
            Either.catch {
                com.example.generated.Player.select { com.example.generated.Player.id eq id }.map {
                    Player(
                        id = it[com.example.generated.Player.id],
                        name = it[com.example.generated.Player.account]
                    )
                }.first()
            }
        }
    }

    override fun save(player: Player): Either<Throwable, Player> {

            return transaction {
                fetch(player.id).fold(
                    ifLeft = {
                        Either.catch {
                            com.example.generated.Player.insert {
                                it[id] = player.id
                                it[account] = player.name
                            }.resultedValues!!.map {
                                Player(
                                    id = it[com.example.generated.Player.id],
                                    name = it[com.example.generated.Player.account]
                                )
                            }.first()
                        }
                    },
                    ifRight = {
                        Either.catch {
                            com.example.generated.Player.update(
                                {com.example.generated.Player.id.eq(player.id)}
                            ) {
                                it[account] = player.name
                            }
                        }.flatMap {  fetch(player.id) }

                    }
                )

            }
    }

}
