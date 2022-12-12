package com.example.test

import com.example.AuthService
import com.example.AuthServiceImpl
import com.example.PlayerService
import com.example.PlayerServiceImpl
import com.example.ScoreService
import com.example.ScoreServiceImpl
import com.example.TournamentService
import com.example.TournamentServiceImpl
import com.example.configureKoin
import com.example.configureRouting
import com.example.configureSecurity
import com.example.configureSerialization
import com.example.dao.PlayerDao
import com.example.dao.PlayerDaoImpl
import com.example.dao.ScoreDao
import com.example.dao.ScoreDaoImpl
import com.example.dao.TournamentDao
import com.example.dao.TournamentDaoImpl
import com.example.dao.UserDao
import com.example.dao.UserDaoImpl
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.koin.core.module.Module
import org.koin.dsl.module
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.test.assertEquals

@TestMethodOrder(OrderAnnotation::class)
class ApplicationTest {




    fun injectTestConf(): Module {
        return module {
            single { UserDaoImpl() as UserDao }
            single { AuthServiceImpl(get()) as AuthService }
            single { PlayerDaoImpl() as PlayerDao }
            single { PlayerServiceImpl(get()) as PlayerService}
            single { TournamentDaoImpl() as TournamentDao }
            single { TournamentServiceImpl(get()) as TournamentService}
            single { ScoreDaoImpl() as ScoreDao }
            single { ScoreServiceImpl(get(), get(), get()) as ScoreService }
        }
    }

    fun configureTestDb() {

        val hikariConfig = HikariConfig(this::class.java.classLoader.getResource("db-test.properties")!!.path)
        val dataSource = HikariDataSource(hikariConfig)

        val flyway = Flyway.configure().dataSource(dataSource).load()
        flyway.migrate()

        Database.connect(dataSource)

    }

    @Test
    fun testRoot() = testApplication {


        application {
            configureRouting()
            configureSerialization()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testUnAuthorized() = testApplication {
        val basicClient = createClient {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "jetbrains", password = "jetbrains")
                    }
                    realm = "Ktor Server"
                }
            }
        }
        application {
            configureKoin(injectTestConf())
            configureRouting()
            configureSerialization()
            configureSecurity()
            configureTestDb()
        }
        basicClient.get("/player").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)

        }
    }

    @Test
    @Order(1)
    fun `add a player`() = testApplication {
        val basicClient = createClient {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "admin", password = "test")
                    }
                    realm = "Ktor Server"
                }
            }
        }
        application {
            configureKoin(injectTestConf())
            configureRouting()
            configureSerialization()
            configureSecurity()
            configureTestDb()
        }
        basicClient.post("/player/", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(PlayerDto(id = 1, name = "Christophe")))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(PlayerDto(1, "Christophe"), jacksonObjectMapper().readValue(bodyAsText(), PlayerDto::class.java))
        }
        basicClient.get("/player").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(1, jacksonObjectMapper().readValue(bodyAsText(), Array<PlayerDto>::class.java).filter { it.id == 1}.size)

        }
        basicClient.post("/player/", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(PlayerDto(id = 1, name = "Christophe modified")))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(PlayerDto(1, "Christophe modified"), jacksonObjectMapper().readValue(bodyAsText(), PlayerDto::class.java))
        }
        basicClient.get("/player").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(1, jacksonObjectMapper().readValue(bodyAsText(), Array<PlayerDto>::class.java).filter { it.name == "Christophe modified"}.size)

        }
    }
    data class PlayerDto(val id: Int, val name: String)



    @Test
    @Order(2)
    fun `add a tournament`() = testApplication {
        val basicClient = createClient {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "admin", password = "test")
                    }
                    realm = "Ktor Server"
                }
            }
        }
        application {
            configureKoin(injectTestConf())
            configureRouting()
            configureSerialization()
            configureSecurity()
             configureTestDb()
        }
        basicClient.post("/tournament/", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(TournamentDto(id = 1, name = "casino royal")))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(TournamentDto(1, "casino royal"), jacksonObjectMapper().readValue(bodyAsText(), TournamentDto::class.java))
        }
        basicClient.get("/tournament").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(1, jacksonObjectMapper().readValue(bodyAsText(), Array<TournamentDto>::class.java).filter { it.id == 1}.size)

        }
        basicClient.post("/tournament/", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(TournamentDto(id = 1, name = "Blackjack")))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(TournamentDto(1, "Blackjack"), jacksonObjectMapper().readValue(bodyAsText(), TournamentDto::class.java))
        }
        basicClient.get("/tournament").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(1, jacksonObjectMapper().readValue(bodyAsText(), Array<TournamentDto>::class.java).filter { it.name == "Blackjack"}.size)

        }
    }
    data class TournamentDto(val id: Int, val name: String)



    @Test
    @Order(3)
    fun `a player subscribe a tournament`() = testApplication {
        val basicClient = createClient {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "admin", password = "test")
                    }
                    realm = "Ktor Server"
                }
            }
        }
        application {
            configureKoin(injectTestConf())
            configureRouting()
            configureSerialization()
            configureSecurity()
            configureTestDb()
        }
        basicClient.post("/tournament/", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(TournamentDto(id = 1, name = "casino royal")))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(TournamentDto(1, "casino royal"), jacksonObjectMapper().readValue(bodyAsText(), TournamentDto::class.java))
        }
        basicClient.post("/player/", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(PlayerDto(id = 1, name = "Christophe")))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(PlayerDto(1, "Christophe"), jacksonObjectMapper().readValue(bodyAsText(), PlayerDto::class.java))
        }
        basicClient.post("/tournament/1/player/1", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(PointDto(amount = 0)))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                TournamentPlayerDto(tournamentId = 1, playerId = 1, amount = 0, rank = 1),
                jacksonObjectMapper().readValue(bodyAsText(), TournamentPlayerDto::class.java))
        }
        basicClient.get("/tournament/1/player/1", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                TournamentPlayerDto(tournamentId = 1, playerId = 1, amount = 0, rank = 1),
                jacksonObjectMapper().readValue(bodyAsText(), TournamentPlayerDto::class.java))
        }

    }

    @Test
    @Order(4)
    fun `a player win point into a tournament`() = testApplication {
        val basicClient = createClient {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "admin", password = "test")
                    }
                    realm = "Ktor Server"
                }
            }
        }
        application {
            configureKoin(injectTestConf())
            configureRouting()
            configureSerialization()
            configureSecurity()
             configureTestDb()
        }
        basicClient.post("/tournament/", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(TournamentDto(id = 2, name = "casino")))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(TournamentDto(2, "casino"), jacksonObjectMapper().readValue(bodyAsText(), TournamentDto::class.java))
        }

        basicClient.post("/tournament/2/player/1", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(PointDto(amount = 2)))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                TournamentPlayerDto(tournamentId = 2, playerId = 1, amount = 2, rank = 1),
                jacksonObjectMapper().readValue(bodyAsText(), TournamentPlayerDto::class.java))
        }
        basicClient.post("/tournament/2/player/1", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(PointDto(amount = 1)))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                TournamentPlayerDto(tournamentId = 2, playerId = 1, amount = 3, rank = 1),
                jacksonObjectMapper().readValue(bodyAsText(), TournamentPlayerDto::class.java))
        }
        basicClient.get("/tournament/2/player/1", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                TournamentPlayerDto(tournamentId = 2, playerId = 1, amount = 3, rank = 1),
                jacksonObjectMapper().readValue(bodyAsText(), TournamentPlayerDto::class.java))
        }
        basicClient.delete("/tournament/2", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }



    @Test
    @Order(5)
    fun `several player win point into a tournament and return scoreBoard`() = testApplication {
        val basicClient = createClient {
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = "admin", password = "test")
                    }
                    realm = "Ktor Server"
                }
            }
        }
        application {
            configureKoin(injectTestConf())
            configureRouting()
            configureSerialization()
            configureSecurity()
             configureTestDb()
        }
        basicClient.post("/tournament/", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(TournamentDto(id = 3, name = "casino")))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(TournamentDto(3, "casino"), jacksonObjectMapper().readValue(bodyAsText(), TournamentDto::class.java))
        }
        basicClient.post("/player/", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(PlayerDto(id = 2, name = "John Doe")))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(PlayerDto(2, "John Doe"), jacksonObjectMapper().readValue(bodyAsText(), PlayerDto::class.java))
        }

        basicClient.post("/tournament/3/player/1", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(PointDto(amount = 4)))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                TournamentPlayerDto(tournamentId = 3, playerId = 1, amount = 4, rank = 1),
                jacksonObjectMapper().readValue(bodyAsText(), TournamentPlayerDto::class.java))
        }
        basicClient.post("/tournament/3/player/2", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jacksonObjectMapper().writeValueAsString(PointDto(amount = 6)))
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                TournamentPlayerDto(tournamentId = 3, playerId = 2, amount = 6, rank = 1),
                jacksonObjectMapper().readValue(bodyAsText(), TournamentPlayerDto::class.java))
        }
        basicClient.get("/tournament/3/player/1", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals(
                TournamentPlayerDto(tournamentId = 3, playerId = 1, amount = 4, rank = 2),
                jacksonObjectMapper().readValue(bodyAsText(), TournamentPlayerDto::class.java))
        }
        basicClient.get("/tournament/3", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        basicClient.delete("/tournament/3", ) {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }


    data class PointDto(val amount: Int)
    data class TournamentPlayerDto(val tournamentId: Int, val playerId: Int, val amount: Int, val rank: Int)

    @Test
    fun testCrypt(){
        val salt = Base64.getDecoder().decode("czIkMclyVVPHw8Gq7fdvQQ==")
        val spec: KeySpec = PBEKeySpec("test".toCharArray(), salt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded

        val spec2: KeySpec = PBEKeySpec("test".toCharArray(), salt, 65536, 128)
        val factory2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash2 = factory2.generateSecret(spec2).encoded

        assertEquals(String(hash),String(hash2))
    }
}
