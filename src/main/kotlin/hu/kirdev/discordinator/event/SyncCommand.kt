package hu.kirdev.discordinator.event

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent
import discord4j.rest.util.Image
import hu.kirdev.discordinator.service.ServerService
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

fun handleSync(event: ApplicationCommandInteractionEvent, serverService: ServerService): Mono<Void> {
    return Mono.just(event)
        .flatMap { it.interaction.guild }
        .flatMap { guild -> Mono.just(guild)
                .flatMapMany { guild.roles }
                .collectList()
                .publishOn(Schedulers.boundedElastic())
                .map { roles ->
                    serverService.syncServer(
                            name      = guild.name,
                            discordId = guild.id.asLong(),
                            logo      = guild.getIconUrl(Image.Format.PNG).orElse(""),
                            roles     = mapRoles(roles))
                }
                .flatMap { event.reply()
                        .withEphemeral(true)
                        .withContent(it)
                }
        }
        .then()
}