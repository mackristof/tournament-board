package com.example

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.getOrHandle
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


fun Application.configureRouting() {

    val playerService: PlayerService by inject()
    val tournamentService: TournamentService by inject()
    val scoreService: ScoreService by inject()

    routing {
        get("/") {
            call.respondText { "Welcome in tournament board" }
        }
        authenticate("auth") {
            get("/player") {
                call.respond(playerService.list().getOrHandle { it })
            }
            post("/player/") {
                val player = call.receive<Player>()
                call.respond(playerService.add(player).getOrHandle { it })
            }
            get("/tournament") {
                call.respond(tournamentService.list().getOrHandle { it })
            }
            post("/tournament/") {
                val tournament = call.receive<Tournament>()
                call.respond(tournamentService.add(tournament).getOrHandle { it })
            }
            get("/tournament/{tournamentId}") {
                val tournament = Either.catch {
                    call.parameters["tournamentId"]?.toInt() ?: throw IllegalArgumentException("tournamentId must be Int")
                }
                either {
                    call.respond(scoreService.getBoardScore(tournament.bind()).bind())
                }.getOrHandle { it }
            }
            delete("/tournament/{tournamentId}") {
                val tournament = Either.catch {
                    call.parameters["tournamentId"]?.toInt() ?: throw IllegalArgumentException("tournamentId must be Int")
                }
                either {
                    call.respond(tournamentService.delete(tournament.bind()))
                }.getOrHandle { it }
            }
            get("/tournament/{tournamentId}/player/{playerId}") {
                val tournament = Either.catch {
                    call.parameters["tournamentId"]?.toInt() ?: throw IllegalArgumentException("tournamentId must be Int")
                }
                val player = Either.catch {
                    call.parameters["playerId"]?.toInt() ?: throw IllegalArgumentException("playerId must be Int")
                }
                either {
                    val score = scoreService.get(tournament.bind(), player.bind()).bind()
                    call.respond(score)
                }.getOrHandle { it }
            }
            post("/tournament/{tournamentId}/player/{playerId}") {
                val tournament = Either.catch {
                    call.parameters["tournamentId"]?.toInt() ?: throw IllegalArgumentException("tournamentId must be Int")
                }
                val player = Either.catch {
                    call.parameters["playerId"]?.toInt() ?: throw IllegalArgumentException("playerId must be Int")
                }
                val point = call.receive<Point>()
                either {
                    val score = scoreService.registerScore(tournament.bind(), player.bind(), point.amount).bind()
                    call.respond(score)
                }.getOrHandle { it }

            }
        }
    }
}
