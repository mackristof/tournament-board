package com.example


data class User(val login: String)
data class Player(val id : Int, val name: String)
data class Tournament(val id : Int, val name: String)
data class Point(val amount: Int)
data class Score(val tournamentId: Int, val playerId: Int, val amount: Int, val rank: Int)

