package com.github.clojj.aks.demoserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient


@EnableDiscoveryClient
@SpringBootApplication
class DemoServerApplication {

    @Bean
    fun webClient(): WebClient {
        return WebClient.create()
    }

}

fun main(args: Array<String>) {
    runApplication<DemoServerApplication>(*args)
}

@RestController
@RequestMapping("/demo")
class Controller(val webClient: WebClient) {

    @GetMapping("/hello")
    fun hello(): String {
        val url = "http://astronomy-service:8080/stars/test"
        val response = webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
        return "response from astronomy-service via Discovery Client and WebClient $webClient $response"
    }

}