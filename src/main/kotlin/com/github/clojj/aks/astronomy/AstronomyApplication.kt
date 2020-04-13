package com.github.clojj.aks.astronomy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class AstronomyApplication

fun main(args: Array<String>) {
    runApplication<AstronomyApplication>(*args)
}

@RestController
@RequestMapping("/stars")
class Controller {

    @GetMapping("/test")
    fun test(): String {
        return "so many stars..."
    }

}