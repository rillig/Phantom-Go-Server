package de.roland_illig.phantomgoserver.domain

import java.time.LocalDateTime

interface GameService {
    fun create(id: String, black: String, white: String): GameDto
    fun read(gameId: String): GameDto?
    fun updateMoves(id: String, move: String): GameDto?
    fun delete(id: String)
    fun list(): List<GameDto>
}

data class GameDto(
        val id: String,
        val black: String,
        val white: String,
        val moves: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime)
