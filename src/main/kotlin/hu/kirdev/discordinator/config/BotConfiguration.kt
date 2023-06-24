package hu.kirdev.discordinator.config

import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.Event
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import hu.kirdev.discordinator.event.AdminCommandListener
import hu.kirdev.discordinator.event.EventListener
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class BotConfiguration(
        private val config: AppConfig,
        private val client: GatewayDiscordClient
) {


    @PostConstruct
    fun init() {
        log.info("Token length: {}", config.token.length)
    }

    @PreDestroy
    fun clean() {
        log.info("Shutting down")
    }

    @Bean
    fun <T : Event> gatewayDiscordClient(eventListeners: List<EventListener<T>>): GatewayDiscordClient {
        for (listener in eventListeners) {
            client.on(listener.eventType)
                    .flatMap(listener::execute)
                    .onErrorResume(listener::handleError)
                    .subscribe()
        }

        val applicationId = client.restClient.applicationId.block()!!
        AdminCommandListener.registerCommand(client, applicationId)

//        val commands = client.restClient.applicationService.getGlobalApplicationCommands(applicationId).collectList().block()
//        val commandId = commands?.find { println(it); it.name() == "discoordinate" }?.id()?.asLong() ?: 0
//        client.restClient.applicationService.deleteGlobalApplicationCommand(applicationId, commandId).subscribe()

        return client
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(BotConfiguration::class.java)
    }
}