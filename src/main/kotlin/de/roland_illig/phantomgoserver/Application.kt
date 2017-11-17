package de.roland_illig.phantomgoserver

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InjectionPoint
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Scope

@SpringBootApplication
class Application {

    @Bean
    @Scope("prototype")
    fun logger(injectionPoint: InjectionPoint): Logger
            = LoggerFactory.getLogger(injectionPoint.methodParameter.containingClass)

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
                .registerModule(KotlinModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
