package de.roland_illig.phantomgoserver.web

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import de.roland_illig.phantomgoserver.domain.GameDto
import de.roland_illig.phantomgoserver.domain.GameService
import org.slf4j.Logger
import org.springframework.hateoas.ResourceSupport
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo
import org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn
import org.springframework.http.HttpEntity
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder

@RestController
@RequestMapping(
        value = "games",
        consumes = arrayOf(
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.TEXT_XML_VALUE,
                MediaType.APPLICATION_XML_VALUE),
        produces = arrayOf(
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.TEXT_XML_VALUE,
                MediaType.APPLICATION_XML_VALUE))
class GameController(val service: GameService, val log: Logger) {

    @PostMapping
    fun create(@RequestBody req: CreateGameRequest, uriBuilder: UriComponentsBuilder)
            : HttpEntity<GameResource> {
        log.debug("Creating game {}", req)

        val dto = service.create(req._id, req.black, req.white)

        val resource = dto.toResource()
        // FIXME: the below code looks redundant
        val uri = uriBuilder.path("games/{id}").buildAndExpand(resource._id).toUri()
        return ResponseEntity.created(uri).body(resource)
    }


    @GetMapping
    fun list(): HttpEntity<Resources<GameResource>> {
        log.debug("Listing games")

        val dtos = service.list()

        return ResponseEntity.ok(Resources(dtos.map { it.toResource() }))
    }

    @GetMapping("{id}")
    fun read(@PathVariable id: String): HttpEntity<GameResource> {
        log.debug("Reading game {}", id)

        val dto = service.read(id)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(dto.toResource())
    }

    @PostMapping("{id}")
    fun updateMoves(@PathVariable id: String, @RequestBody move: MoveResource): HttpEntity<GameResource> {
        log.debug("Updating game {} with {}", id, move)

        val dto = service.updateMoves(id, move.move)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(dto.toResource())
    }

    fun GameDto.toResource(): GameResource {
        val resource = GameResource(_id = id, black = black, white = white)
        resource.add(linkTo(methodOn(GameController::class.java).read(resource._id)).withSelfRel())
        return resource
    }
}

data class GameResource @JsonCreator constructor(
        @JsonProperty("id") val _id: String,
        @JsonProperty val black: String,
        @JsonProperty val white: String)
    : ResourceSupport()

typealias CreateGameRequest = GameResource

data class MoveResource @JsonCreator constructor(
        @JsonProperty val move: String)
    : ResourceSupport()
