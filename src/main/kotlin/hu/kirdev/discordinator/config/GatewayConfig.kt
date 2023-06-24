package hu.kirdev.discordinator.config

import discord4j.core.DiscordClientBuilder
import discord4j.core.GatewayDiscordClient
import discord4j.gateway.intent.Intent
import discord4j.gateway.intent.IntentSet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig(
        private val config: AppConfig
) {

    @Bean
    fun client(): GatewayDiscordClient {
        return DiscordClientBuilder.create(config.token)
                .build()
                .gateway()
                .setEnabledIntents(IntentSet.of(
                        Intent.GUILD_MESSAGES,
                        Intent.GUILD_MESSAGE_REACTIONS,
                        Intent.GUILD_MODERATION,
                        Intent.GUILDS
                ))
                .login()
                .block()!!
    }

}