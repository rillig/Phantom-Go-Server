package de.roland_illig.phantomgoserver.web

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.verify
import de.roland_illig.phantomgoserver.domain.PlayerDto
import de.roland_illig.phantomgoserver.domain.PlayerService
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
@WebMvcTest(PlayerController::class)
class PlayerControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var playerService: PlayerService

    @TestConfiguration
    class Config {
        @Bean
        fun playerService(): PlayerService = Mockito.mock(PlayerService::class.java)
    }

    @Before
    fun setup() {
        reset(playerService)
    }

    @Test
    fun `Retrieving an unknown player should result in status 404`() {
        mockMvc.perform(MockMvcRequestBuilders.get("/players/unknown")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    fun `Creating a player with an invalid request body should result in status 400 `() {
        mockMvc.perform(MockMvcRequestBuilders.post("/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `Creating a player with a valid request body should result in status 201 and a location header`() {
        val now = LocalDateTime.now()
        Mockito.`when`(playerService.create(any()))
                .thenReturn(PlayerDto("player", now, now, "playername"))

        mockMvc.perform(MockMvcRequestBuilders.post("/players")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"id":"player", "name":"Playername"}"""))
                .andExpect(MockMvcResultMatchers.status().isCreated)
                .andExpect(MockMvcResultMatchers.header().string("location", "http://localhost/players/player"))

        verify(playerService).create(any())
    }

    @Test
    fun `Successfully updating a player should result in status 200`() {
        Mockito.`when`(playerService.updateName(any(), any()))
                .thenAnswer { PlayerDto("playerId", LocalDateTime.now(), LocalDateTime.now(), it.getArgument(1)) }

        mockMvc.perform(MockMvcRequestBuilders.post("/players/playerId")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"name":"Playername"}"""))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.equalTo("playerId")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.equalTo("Playername")))

        verify(playerService).updateName(eq("playerId"), eq("Playername"))
    }
}
