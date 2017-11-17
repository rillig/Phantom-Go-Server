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
internal class PlayerConfig

@Service
@Transactional
internal class JpaPlayerService(val repo: PlayerRepository, val log: Logger) : PlayerService {

    override fun create(player: CreatePlayerDto): PlayerDto {
        log.debug("Creating player {}", player)

        val now = LocalDateTime.now()
        val entity = PlayerEntity(
                id = player.id,
                createdAt = now,
                updatedAt = now,
                name = player.name)
        if (repo.exists(player.id)) {
            throw DuplicateKeyException(player.id)
        }
        return repo.save(entity).toDto()
    }

    override fun read(id: String): PlayerDto? {
        log.debug("Reading player {}", id)

        return repo.findOne(id)?.toDto()
    }

    override fun updateName(id: String, name: String): PlayerDto? {
        log.debug("Updating player {} with name {}", id, name)

        val existing = repo.findOne(id) ?: return null
        val updated = existing.copy(
                updatedAt = LocalDateTime.now(),
                name = name)
        val saved = repo.save(updated)
        return saved.toDto()
    }

    override fun delete(id: String) {
        log.debug("Deleting player {}", id)

        repo.delete(id)
    }

    override fun list(): List<PlayerDto> {
        log.debug("Listing players")

        return repo.findAll().map { it.toDto() }
    }

    private fun PlayerEntity.toDto(): PlayerDto = PlayerDto(
            id = this.id!!,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            name = this.name)
}

@Repository
@Transactional(Transactional.TxType.MANDATORY)
internal interface PlayerRepository : JpaRepository<PlayerEntity, String>

@Entity
@Table(name = "player")
internal data class PlayerEntity(
        @Id val id: String?,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val name: String) {

    /** For JPA. */
    private constructor() : this(
            id = null,
            createdAt = LocalDateTime.MIN,
            updatedAt = LocalDateTime.MIN,
            name = "")
}
