package de.roland_illig.phantomgoserver.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.JUnitSoftAssertions
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope
import org.springframework.dao.DuplicateKeyException
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = arrayOf(
        JpaPlayerServiceTest.Config::class,
        PlayerConfig::class))
@DataJpaTest
@Transactional(propagation = Propagation.NEVER)
@Rollback
internal class JpaPlayerServiceTest {

    class Config {
        @Bean
        @Scope("prototype")
        fun logger(): Logger = mock(Logger::class.java)
    }

    @Autowired
    private lateinit var service: PlayerService

    @get:Rule
    private val softly = JUnitSoftAssertions()

    @After
    fun tearDown() {
        service.list().forEach { service.delete(it.id) }
    }

    @Test
    fun `'create' returns created player`() {
        val (id, _, _, name) = service.create(CreatePlayerDto("id", "name"))

        softly.assertThat(id).isEqualTo("id")
        softly.assertThat(name).isEqualTo("name")
    }

    @Test
    fun `'create' cannot create duplicate players`() {
        service.create(CreatePlayerDto("id", "name"))

        softly.assertThatThrownBy { service.create(CreatePlayerDto("id", "name")) }
                .isInstanceOf(DuplicateKeyException::class.java)
                .hasMessage("game")
    }

    @Test
    fun `'read' returns null if a player doesn't exist`() {
        val result = service.read("invalid")

        assertThat(result).isNull()
    }

    @Test
    fun `'read' returns an existing player`() {
        service.create(CreatePlayerDto("player", "playername"))

        val result = service.read("player")

        softly.assertThat(result?.id).isNotNull
        softly.assertThat(result?.name).isEqualTo("playername")
    }

    @Test
    fun `'updateName' updates the name, and only that`() {
        val existingPlayer = service.create(CreatePlayerDto("player", "playername"))

        Thread.sleep(1)

        val result = service.updateName(existingPlayer.id, "new name")

        softly.assertThat(result).isNotNull
        softly.assertThat(result?.id).isEqualTo(existingPlayer.id)
        softly.assertThat(result?.name).isEqualTo("new name")
        softly.assertThat(result?.updatedAt).isAfter(existingPlayer.updatedAt)
        softly.assertThat(result?.createdAt).isEqualTo(existingPlayer.createdAt)
    }

    @Test
    fun `'updateName' for a nonexistent player returns null`() {
        service.create(CreatePlayerDto("player", "playername"))

        val result = service.updateName("nonexistent", "new name")

        softly.assertThat(result).isNull()
    }

    @Test
    fun `'delete' deletes a player`() {
        service.create(CreatePlayerDto("player", "playername"))

        val existing = service.read("player")

        softly.assertThat(existing).isNotNull

        service.delete("player")

        val nonexistent = service.read("player")

        softly.assertThat(nonexistent).isNull()
    }

    @Test
    fun `'list' on an empty repository returns an empty list`() {
        val result = service.list()

        assertThat(result).isEmpty()
    }

    @Test
    fun `'list' returns existing players`() {
        service.create(CreatePlayerDto("player", "playername"))

        val result = service.list()

        softly.assertThat(result).hasSize(1)
        result.forEach {
            softly.assertThat(it.id).isNotNull
            softly.assertThat(it.name).isEqualTo("playername")
        }
    }
}
