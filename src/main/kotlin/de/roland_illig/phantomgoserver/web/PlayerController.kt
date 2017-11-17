package de.roland_illig.phantomgoserver.web

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import de.roland_illig.phantomgoserver.domain.CreatePlayerDto
import de.roland_illig.phantomgoserver.domain.PlayerDto
import de.roland_illig.phantomgoserver.domain.PlayerService
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
        value = "players",
        produces = arrayOf(
                MediaType.APPLICATION_JSON_VALUE,
                MediaType.TEXT_XML_VALUE,
                MediaType.APPLICATION_XML_VALUE))
class PlayerController(val service: PlayerService, val log: Logger) {

    @PostMapping(consumes = arrayOf(
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_XML_VALUE,
            MediaType.APPLICATION_XML_VALUE))
    fun create(@RequestBody request: CreatePlayerDto, uriBuilder: UriComponentsBuilder): HttpEntity<PlayerResource> {
        log.debug("Creating player {}", request)

        val dto = service.create(request)

        val resource = dto.toResource()
        val linkToSelf = linkTo(methodOn(this::class.java).read(dto.id))
        resource.add(linkToSelf.withSelfRel())
        return ResponseEntity.created(uriBuilder.path("players/{id}").buildAndExpand(dto.id).toUri()).body(resource)
    }


    @GetMapping
    fun list(): HttpEntity<Resources<PlayerResource>> {
        log.debug("Listing players")

        val dtos = service.list()

        return ResponseEntity.ok(Resources(dtos.map { it.toResource() }))
    }

    @GetMapping("{id}")
    fun read(@PathVariable id: String): HttpEntity<PlayerResource> {
        log.debug("Reading player {}", id)

        val dto = service.read(id)
                ?: return ResponseEntity.notFound().build()

        val resource = dto.toResource()
        resource.add(linkTo(methodOn(this::class.java).read(dto.id)).withSelfRel())
        return ResponseEntity.ok(resource)
    }

    @PostMapping("{id}")
    fun updateName(@PathVariable id: String, @RequestBody req: UpdateNameRequest)
            : HttpEntity<PlayerResource> {
        log.debug("Updating player {} with {}", id, req)

        val dto = service.updateName(id, req.name)
                ?: return ResponseEntity.notFound().build()

        val resource = dto.toResource()
        resource.add(linkTo(methodOn(this::class.java).read(dto.id)).withSelfRel())
        return ResponseEntity.ok(resource)
    }

    fun PlayerDto.toResource() = PlayerResource(id, name)
}

data class PlayerResource @JsonCreator constructor(
        @JsonProperty("id") val _id: String,
        @JsonProperty val name: String)
    : ResourceSupport()

data class UpdateNameRequest @JsonCreator constructor(
        @JsonProperty val name: String)
    : ResourceSupport()
