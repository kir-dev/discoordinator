package hu.kirdev.discordinator.event

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent
import discord4j.core.`object`.entity.Role
import discord4j.rest.util.Color
import discord4j.rest.util.Image
import hu.kirdev.discordinator.APP_NAME
import hu.kirdev.discordinator.config.Lang
import hu.kirdev.discordinator.model.RoleEntity
import hu.kirdev.discordinator.service.InviteService
import hu.kirdev.discordinator.service.ServerService
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

fun handleSetup(
        event: ApplicationCommandInteractionEvent,
        code: String,
        invites: InviteService,
        serverService: ServerService
): Mono<Void> {
    val server = invites.popLinkForSetup(code)
    return if (server != null) {
        Mono.just(event)
            .flatMap { it.interaction.guild }
            .flatMap { guild -> Mono.just(guild)
                .flatMapMany { guild.roles }
                .collectList()
                .publishOn(Schedulers.boundedElastic())
                .map { roles ->
                    serverService.linkServer(
                            server    = server,
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
    } else {
        event.reply()
            .withEphemeral(true)
            .withContent(Lang.invalidCode)
    }
}

fun mapRoles(roles: List<Role>): List<RoleEntity> {
    val botRolePosition = roles
        .filter { it.name == APP_NAME }
        .map { it.data.position() }
        .firstOrNull() ?: 0
    return roles
        .filter { !it.isEveryone }
        .filter { it.data.position() < botRolePosition }
        .map { RoleEntity(it.id.asLong(), it.name, mapColor(it.color), it.data.position()) }
}

private fun mapColor(it: Color) =
    String.format("#%02x%02x%02x", it.red, it.green, it.blue)