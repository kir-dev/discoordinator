package hu.kirdev.discordinator.event

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent
import discord4j.core.`object`.reaction.ReactionEmoji
import hu.kirdev.discordinator.config.Lang
import hu.kirdev.discordinator.service.ServerService
import reactor.core.publisher.Mono

fun handleMessage(event: ApplicationCommandInteractionEvent, serverService: ServerService): Mono<Void> {
    return Mono.just(event)
        .flatMap { it.interaction.guild }
        .map { guild -> serverService.getServer(guild.id) }
        .flatMap { server ->
            if (server.isEmpty) {
                event.reply(Lang.notConnected).withEphemeral(true)
            } else {
                Mono.just(event)
                    .flatMap { it.reply("...").withEphemeral(true) }
                    .then(event.interactionResponse.deleteInitialResponse())
                    .then(event.interaction.channel)
                    .flatMap { it.createMessage(server.orElseThrow().reactMessage) }
                    .flatMap { it.addReaction(ReactionEmoji.unicode("\u26A1")) }
            }
        }
}