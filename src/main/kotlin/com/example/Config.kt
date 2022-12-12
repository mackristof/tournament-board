package com.example

import com.example.dao.PlayerDao
import com.example.dao.PlayerDaoImpl
import com.example.dao.ScoreDao
import com.example.dao.ScoreDaoImpl
import com.example.dao.TournamentDao
import com.example.dao.TournamentDaoImpl
import com.example.dao.UserDao
import com.example.dao.UserDaoImpl
import com.fasterxml.jackson.databind.SerializationFeature
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.koin.core.module.Module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.util.*

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
}

fun Application.configureSecurity() {

    val authService: AuthService by inject()
    val salt = Base64.getDecoder().decode("czIkMclyVVPHw8Gq7fdvQQ==")

    authentication {
        basic(name = "auth") {
            realm = "Ktor Server"
            validate { credentials ->
                authService.authenticate(credentials.name, credentials.password, salt).orNull()
            }
        }
    }

}


fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        anyHost()
    }
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }


}

fun Application.configureDb(){
    val confFile = this::class.java.classLoader.getResourceAsStream("db.properties")
    val prop = Properties()
    prop.load(confFile)
    val hikariConfig = HikariConfig(prop)
    val dataSource = HikariDataSource(hikariConfig)

    val flyway = Flyway.configure().dataSource(dataSource).load()
    flyway.migrate()

    Database.connect(dataSource)
}

fun Application.injectConf(): Module {
    return org.koin.dsl.module {
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

fun Application.configureKoin(module: Module) {

    install(Koin) {
        slf4jLogger()
        modules(module)
    }
}

