package com.github.clojj.aks.astronomy

import kotlinx.coroutines.*
import kotlinx.coroutines.NonCancellable.isActive
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.concurrent.Executors
import javax.annotation.PreDestroy
import kotlin.time.ExperimentalTime


@SpringBootApplication
class AstronomyApplication

fun main(args: Array<String>) {
    runApplication<AstronomyApplication>(*args)
}

@ExperimentalTime
@InternalCoroutinesApi
@RestController
@RequestMapping("/stars")
class Controller(val coScheduler: CoScheduler) {

    @GetMapping("/test")
    fun test(): String {
        return "so many stars..."
    }

    @GetMapping("/cancel")
    fun cancel() {
        coScheduler.cancel()
    }

}

@InternalCoroutinesApi
@ExperimentalTime
suspend inline fun <T> scheduleRepeating(
    from: Long,
    interval: Long,
    crossinline action: (T?) -> T?
) {
    delay(from)
    var result: T? = null
    while (isActive) {
        result = action(result)
        delay(interval)
    }
}


@InternalCoroutinesApi
@ExperimentalTime
@Component
class CoScheduler(val configProperties: ConfigProperties) {

    private val GITHUB_V3_MIME_TYPE = "application/vnd.github.v3+json"
    private val GITHUB_API_BASE_URL = "https://api.github.com"

    private val restTemplate = RestTemplate()

    private val scope = CoroutineScope(Job() + Executors.newSingleThreadExecutor().asCoroutineDispatcher() + CoroutineName("parent scope for scheduled job"))

    private lateinit var schedulerJob: Job

    val headers = HttpEntity<Any>(HttpHeaders().apply {
        accept = listOf(MediaType.APPLICATION_JSON)
        contentType = MediaType.parseMediaType(GITHUB_V3_MIME_TYPE)
        setBasicAuth(HttpHeaders.encodeBasicAuth("clojj", configProperties.token, null))
    })

    val retrieveStarredPage: (String?) -> String? =
        { next: String? ->
            // TODO handle errors and timeout
            val url = next ?: "${GITHUB_API_BASE_URL}/user/starred"
            val responseEntity = restTemplate.exchange(URI(url), HttpMethod.GET, headers, Array<Repository>::class.java)

            // TODO propagate to next invocation
            val linkHeader = responseEntity.headers["Link"]
            linkHeader?.let {
                val next: String? = it[0]
                next?.let {
                    val nextLink = it.split(",").firstOrNull { it.contains("rel=\"next\"") }
                    nextLink?.let {
                        val nextUrl = it.substring(it.indexOf("http"), it.indexOf(">;"))
                        println("next $nextUrl")
                        nextUrl
                    }
                }
            }
        }

    @EventListener
    fun start(event: ContextRefreshedEvent) {
        // TODO Github API rate limiting
        // TODO make interval dynamic
        // TODO pausing job
        // TODO extract coroutine-based scheduling lib
        schedulerJob = with(scope) {
            launch {
                scheduleRepeating(1000, 1000, retrieveStarredPage)
            }
        }
    }

    @PreDestroy
    fun cancel() {
        if (schedulerJob.isActive) {
            println("cancelling job")
            schedulerJob.cancel("cancelled")
        }
    }
}

@Component
@ConfigurationProperties(prefix = "application.github")
data class ConfigProperties(
    var token: String = ""
)


data class User(
    val login: String,
    val id: Long,
    val node_id: String,
    val avatar_url: String,
    val gravatar_id: String,
    val url: String,
    val html_url: String,
    val followers_url: String,
    val following_url: String,
    val gists_url: String,
    val starred_url: String,
    val subscriptions_url: String,
    val organizations_url: String,
    val repos_url: String,
    val events_url: String,
    val received_events_url: String,
    val type: String,
    val site_admin: Boolean
)

data class Repository(
    val id: Long,
    val node_id: String,
    val name: String,
    val full_name: String,
    val owner: User,
    val private: Boolean,
    val html_url: String,
    val description: String?,
    val fork: Boolean,
    val url: String,
    val archive_url: String,
    val assignees_url: String,
    val blobs_url: String,
    val branches_url: String,
    val collaborators_url: String,
    val comments_url: String,
    val commits_url: String,
    val compare_url: String,
    val contents_url: String,
    val contributors_url: String,
    val deployments_url: String,
    val downloads_url: String,
    val events_url: String,
    val forks_url: String,
    val git_commits_url: String,
    val git_refs_url: String,
    val git_tags_url: String,
    val git_url: String,
    val issue_comment_url: String,
    val issue_events_url: String,
    val issues_url: String,
    val keys_url: String,
    val labels_url: String,
    val languages_url: String,
    val merges_url: String,
    val milestones_url: String,
    val notifications_url: String,
    val pulls_url: String,
    val releases_url: String,
    val ssh_url: String,
    val stargazers_url: String,
    val statuses_url: String,
    val subscribers_url: String,
    val subscription_url: String,
    val tags_url: String,
    val teams_url: String,
    val trees_url: String
)
