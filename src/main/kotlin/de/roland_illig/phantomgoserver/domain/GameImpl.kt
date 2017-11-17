package de.roland_illig.phantomgoserver.domain

import org.slf4j.Logger
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.dao.DuplicateKeyException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.transaction.Transactional

@Configuration
@ComponentScan
@EnableJpaRepositories
@EntityScan
@EnableTransactionManagement
internal class GameConfig

@Service
@Transactional
internal class JpaGameService(val repo: GameRepository, val log: Logger) : GameService {

    override fun create(id: String, black: String, white: String): GameDto {
        log.debug("Creating game {}", id)

        val now = LocalDateTime.now()
        val entity = GameEntity(
                id = id,
                black = black,
                white = white,
                moves = "",
                updatedAt = now,
                createdAt = now)
        if (repo.exists(id)) {
            throw DuplicateKeyException(id)
        }
        return repo.save(entity).toDto()
    }

    override fun read(id: String): GameDto? {
        log.debug("Reading game {}", id)

        return repo.findOne(id)?.toDto()
    }

    override fun updateMoves(id: String, move: String): GameDto? {
        log.debug("Updating game {} with move {}", id, move)

        val existing = repo.findOne(id) ?: return null
        val moves = if (existing.moves.isEmpty()) move else existing.moves + " " + move
        val updated = existing.copy(moves = moves, updatedAt = LocalDateTime.now())
        val saved = repo.save(updated)
        return saved.toDto()
    }

    override fun delete(id: String) {
        log.debug("Deleting game {}", id)

        repo.delete(id)
    }

    override fun list(): List<GameDto> {
        log.debug("Listing games")

        return repo.findAll().map { it.toDto() }
    }

    private fun GameEntity.toDto(): GameDto = GameDto(
            id = this.id,
            black = this.black,
            white = this.white,
            moves = this.moves,
            updatedAt = this.updatedAt,
            createdAt = this.createdAt)
}

@Repository
@Transactional(Transactional.TxType.MANDATORY)
internal interface GameRepository : JpaRepository<GameEntity, String>

@Entity
@Table(name = "game")
internal data class GameEntity(
        @Id val id: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val black: String,
        val white: String,
        val moves: String) {

    /** For JPA. */
    private constructor() : this(
            id = "",
            updatedAt = LocalDateTime.MIN,
            createdAt = LocalDateTime.MIN,
            black = "",
            white = "",
            moves = "")
}
