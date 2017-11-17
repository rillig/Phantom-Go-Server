package de.roland_illig.phantomgoserver.web

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import de.roland_illig.phantomgoserver.domain.GameDto
import de.roland_illig.phantomgoserver.domain.GameService
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@WebMvcTest(GameController::class)
class GameControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var gameService: GameService

    @TestConfiguration
    class Config {
        @Bean
        fun gameService(): GameService = Mockito.mock(GameService::class.java)
    }

    @Before
    fun setup() {
        reset(gameService)
    }

    @Test
    fun `Reading an unknown game results in 404`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/games/unknown")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `Creating an invalid game results in 400`() {
        mockMvc.perform(MockMvcRequestBuilders.post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `Creating a valid game results in 201`() {
        val now = LocalDateTime.now()
        Mockito.`when`(gameService.create("gameId", "black", "white"))
                .thenReturn(GameDto("gameId", "black", "white", "", now, now))

        mockMvc.perform(MockMvcRequestBuilders.post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"id":"gameId", "black":"black", "white":"white"}"""))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.header().string("location", "http://localhost/games/gameId"))

        verify(gameService).create("gameId", "black", "white")
    }

    @Test
    fun `Updating a valid game results in 200`() {
        val now = LocalDateTime.now()
        Mockito.`when`(gameService.updateMoves(any(), any()))
                .thenAnswer {
                    val id: String = it.getArgument(0)
                    val move: String = it.getArgument(1)
                    return@thenAnswer GameDto(id, "black", "white", move, now, now)
                }

        mockMvc.perform(MockMvcRequestBuilders.post("/games/gameId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"move":"e5"}"""))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.equalTo("gameId")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.black", CoreMatchers.equalTo("black")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.white", CoreMatchers.equalTo("white")))

        verify(gameService).updateMoves(eq("gameId"), eq("e5"))
    }
}
