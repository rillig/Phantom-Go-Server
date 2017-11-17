package de.roland_illig.phantomgoserver.domain

import org.hibernate.validator.constraints.NotEmpty
import java.time.LocalDateTime

interface PlayerService {
    fun create(player: CreatePlayerDto): PlayerDto
    fun read(id: String): PlayerDto?
    fun updateName(id: String, name: String): PlayerDto?
    fun delete(id: String)
    fun list(): List<PlayerDto>
}

data class PlayerDto(
        val id: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val name: String)

data class CreatePlayerDto(
        @NotEmpty val id: String,
        @NotEmpty val name: String)
