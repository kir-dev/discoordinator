package hu.kirdev.discordinator.event

import discord4j.common.util.Snowflake
import discord4j.core.event.domain.message.ReactionAddEvent
import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.reaction.ReactionEmoji
import discord4j.rest.util.Image
import hu.kirdev.discordinator.config.AppConfig
import hu.kirdev.discordinator.config.Lang
import hu.kirdev.discordinator.model.ServerEntity
import hu.kirdev.discordinator.service.InviteService
import hu.kirdev.discordinator.service.SecureUuid
import hu.kirdev.discordinator.service.ServerService
import hu.kirdev.discordinator.service.UserService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.text.MessageFormat
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class JoinReactEvent(
        private val inviteService: InviteService,
        private val secureUuid: SecureUuid,
        private val config: AppConfig,
        private val userService: UserService,
        private val serverService: ServerService
) : EventListener<ReactionAddEvent> {

    final override val eventType: Class<ReactionAddEvent>
        get() = ReactionAddEvent::class.java

    override fun execute(event: ReactionAddEvent): Mono<Void> {
        return Mono.just(event)
                .filter { it.emoji == ReactionEmoji.unicode("\u26A1") }
                .flatMap { event.message }
                .filter { it.author.orElse(null)?.isBot ?: true
                        && it.author.orElse(null)?.username == "Discoordinator" }
                .flatMap { event.user }
                .filter { !it.isBot }
                .filter { event.guildId.isPresent }
                .flatMap { user ->
                    Mono.just(user)
                        .publishOn(Schedulers.boundedElastic())
                        .map { userService.isUserLinked(user.id) }
                        .flatMap { linked ->
                            if (linked) {
                                handleAlreadyLinked(event, user)
                            } else {
                                handleLinkFirst(user, event.guildId.orElseThrow().asLong())
                            }
                        }
                }
                .then()
    }

    private fun handleAlreadyLinked(event: ReactionAddEvent, user: User): Mono<Void> {
        return Mono.just(event.guildId.orElse(Snowflake.of(0L)) )
                .publishOn(Schedulers.boundedElastic())
                .mapNotNull { serverService.getServer(it).getOrNull() }
                .map { it as ServerEntity }
                .publishOn(Schedulers.boundedElastic())
                .flatMap { server ->
                    return@flatMap addRolesForUser(user, server, event)
                }
                .then()
    }

    private fun addRolesForUser(user: User, server: ServerEntity, event: ReactionAddEvent): Mono<Void> {
        val userEntity = userService.getUserByDiscordId(user.id.asLong())
        val lists = serverService.getAcls(server.id)
        val rules = serverService.getRules(server)
        val roles = resolveRoles(rules, userEntity, lists)
        return addRolesForUser(event.client, server, roles, userEntity)
    }

    private fun handleLinkFirst(user: User, serverId: Long): Mono<Message> {
        val code = inviteService.createLinkForAssociation(
                code = secureUuid.generate(),
                userId = user.id,
                name = user.username + if (user.discriminator == "0") "" else "#${user.discriminator}",
                avatar = user.getAvatarUrl(Image.Format.PNG).orElse(""),
                serverId = serverId)
        return user.privateChannel.flatMap { privateChannel ->
            privateChannel.createMessage(MessageFormat.format(
                    Lang.notLinkedMessage,
                    "${config.baseUrl}/identify/${code}"
            ))
        }
    }
}