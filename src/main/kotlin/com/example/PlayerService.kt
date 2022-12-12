package com.example

import arrow.core.Either
import com.example.dao.PlayerDao

interface PlayerService {
    fun list(): Either<Throwable, List<Player>>
    fun add(player: Player): Either<Throwable, Player>
}


class PlayerServiceImpl(val playerDao: PlayerDao): PlayerService {
    override fun list(): Either<Throwable, List<Player>> {
        return playerDao.list()
    }

    override fun add(player: Player): Either<Throwable, Player> {
        return playerDao.save(player)
    }

}
