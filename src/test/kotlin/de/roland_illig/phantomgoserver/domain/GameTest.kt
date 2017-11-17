package de.roland_illig.phantomgoserver.domain

import org.assertj.core.api.JUnitSoftAssertions
import org.junit.After
import org.junit.Before
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
        JpaGameServiceTest.Config::class,
        GameConfig::class,
        PlayerConfig::class))
@DataJpaTest
@Transactional(propagation = Propagation.NEVER)
@Rollback
internal class JpaGameServiceTest {

    class Config {
        @Bean
        @Scope("prototype")
        fun logger(): Logger = mock(Logger::class.java)
    }

    @Autowired private lateinit var service: GameService

    @Autowired private lateinit var playerService: PlayerService

    @get:Rule
    private val softly = JUnitSoftAssertions()

    @Before
    fun setUp() {
        playerService.create(CreatePlayerDto("blackId", "Black"))
        playerService.create(CreatePlayerDto("whiteId", "White"))
    }

    @After
    fun tearDown() {
        service.list().forEach { service.delete(it.id) }
        playerService.list().forEach { playerService.delete(it.id) }
    }

    @Test
    fun `'create' returns the created game`() {
        val (id, black, white) = service.create("game", "blackId", "whiteId")

        softly.assertThat(id).isEqualTo("game")
        softly.assertThat(black).isEqualTo("blackId")
        softly.assertThat(white).isEqualTo("whiteId")
    }

    @Test
    fun `'create' cannot create duplicate games`() {
        service.create("game", "blackId", "whiteId")

        softly.assertThatThrownBy { service.create("game", "blackId", "whiteId") }
                .isInstanceOf(DuplicateKeyException::class.java)
                .hasMessage("game")
    }

    @Test
    fun `'list' returns an empty list for an empty repo`() {
        val result = service.list()

        softly.assertThat(result).isEmpty()
    }

    @Test
    fun `'list' returns games from a repository`() {
        service.create("game", "blackId", "whiteId")

        val result = service.list()

        softly.assertThat(result)
                .hasSize(1)
                .allSatisfy {
                    softly.assertThat(it.id).isNotEmpty
                    softly.assertThat(it.black).isEqualTo("blackId")
                    softly.assertThat(it.white).isEqualTo("whiteId")
                }
    }

    @Test
    fun `'read' returns null for a nonexistent game`() {
        val result = service.read("nonexistent")

        softly.assertThat(result).isNull()
    }

    @Test
    fun `'read' returns an existing game`() {
        service.create("game", "blackId", "whiteId")

        val result = service.read("game")
        softly.assertThat(result?.id).isNotEmpty
        softly.assertThat(result?.black).isEqualTo("blackId")
        softly.assertThat(result?.white).isEqualTo("whiteId")
    }

    @Test
    fun `'updateMoves' on empty game appends move`() {
        val existingGame = service.create("game", "blackId", "whiteId")

        Thread.sleep(1)

        val result = service.updateMoves(existingGame.id, "move")

        softly.assertThat(result).isNotNull
        softly.assertThat(result?.id).isEqualTo(existingGame.id)
        softly.assertThat(result?.black).isEqualTo("blackId")
        softly.assertThat(result?.white).isEqualTo("whiteId")
        softly.assertThat(result?.moves).isEqualTo("move")
        softly.assertThat(result?.updatedAt).isAfter(existingGame.updatedAt)
        softly.assertThat(result?.createdAt).isEqualTo(existingGame.createdAt)
    }

    @Test
    fun `'updateMoves' on running game appends space and move`() {
        val existingGame = service.create("game", "blackId", "whiteId")
        service.updateMoves(existingGame.id, "move1 move2 move3")

        Thread.sleep(1)

        val result = service.updateMoves(existingGame.id, "move")

        softly.assertThat(result).isNotNull
        softly.assertThat(result?.id).isEqualTo(existingGame.id)
        softly.assertThat(result?.black).isEqualTo("blackId")
        softly.assertThat(result?.white).isEqualTo("whiteId")
        softly.assertThat(result?.moves).isEqualTo("move1 move2 move3 move")
        softly.assertThat(result?.updatedAt).isAfter(existingGame.updatedAt)
        softly.assertThat(result?.createdAt).isEqualTo(existingGame.createdAt)
    }

    @Test
    fun `'delete' deletes an existing game`() {
        service.create("game", "blackId", "whiteId")

        val existing = service.read("game")
        softly.assertThat(existing).isNotNull

        service.delete(existing!!.id)

        val game = service.read(existing.id)

        softly.assertThat(game).isNull()
    }
}
