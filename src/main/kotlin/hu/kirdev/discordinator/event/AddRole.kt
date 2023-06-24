package hu.kirdev.discordinator.event

import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Role
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.PrivateChannel
import discord4j.core.spec.GuildMemberEditSpec
import hu.kirdev.discordinator.config.Lang
import hu.kirdev.discordinator.model.*
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.text.MessageFormat

private val log = LoggerFactory.getLogger(AdminCommandListener::class.java)

fun addRolesForUser(
        client: GatewayDiscordClient,
        server: ServerEntity,
        roleEntities: List<RoleEntity>,
        user: UserEntity
): Mono<Void> {
    return client.getGuildById(Snowflake.of(server.discordId))
        .flatMap { guild -> Mono.just(guild)
            .flatMap { _ -> guild.getMemberById(Snowflake.of(user.discordId)) }
            .flatMap { member: Member ->
                if (server.renameUsers && guild.ownerId.asLong() != user.discordId) {
                    member.edit(GuildMemberEditSpec.builder()
                        .nicknameOrNull("${user.fullName}${if (user.nickname.isNotBlank()) " (${user.nickname})" else ""}")
                        .build())
                } else {
                    Mono.just(member)
                }
            }
            .flatMapMany { member: Member ->
                Flux.fromIterable(roleEntities.map { it.discordId })
                    .flatMap { roleId -> guild.getRoleById(Snowflake.of(roleId)) }
                    .collectList()
                    .flatMap { roles: List<Role> ->
                        member.edit(GuildMemberEditSpec.builder()
                                .addAllRoles(roles.map { it.id })
                                .build())
                        .thenReturn(roles)
                    }
            }
            .flatMap { roles: List<Role> ->
                client.getUserById(Snowflake.of(user.discordId))
                    .flatMap { user: User -> user.privateChannel }
                    .flatMap { channel: PrivateChannel ->
                        log.info("User ${user.fullName} just got roles: ${roles.joinToString(", ") { it.name }}")
                        if (roles.isEmpty()) {
                            channel.createMessage(Lang.gotNoRoleMessage)
                        } else if (roles.size == 1) {
                            channel.createMessage(MessageFormat.format(Lang.gotSingleRoleMessage,
                                    roles.joinToString("`, `") { it.name }))
                        } else {
                            channel.createMessage(MessageFormat.format(Lang.gotMultipleRolesMessage,
                                    roles.joinToString("`, `") { it.name }))
                        }
                    }
            }.then()
        }
        .then()
}

fun resolveRoles(rules: List<RuleEntity>, user: UserEntity, acls: Map<Int, ListEntity>): List<RoleEntity> {
    return rules
        .asSequence()
        .filter { it.enabled }
        .filter { rule -> rule.inPekGroup.isBlank()
            || user.pekGroups.any { it.name == rule.inPekGroup }
        }
        .filter { rule -> rule.inPekGroup.isBlank()
            || !rule.isActiveInPekGroup
            || user.pekGroups.firstOrNull { it.name == rule.inPekGroup }?.end == null
        }
        .filter { rule -> rule.hasPekRole.isBlank()
            || user.pekGroups.firstOrNull { it.name == rule.inPekGroup }?.title?.contains(rule.hasPekRole) ?: false
            || user.pekGroups.firstOrNull { it.name == rule.inPekGroup }?.status == rule.hasPekRole
        }
        .filter { rule -> rule.inAnyAcl.isEmpty()
            || rule.inAnyAcl
                .any { acl ->
                    acls[acl]?.discordNames?.contains(user.discordUsername) ?: false
                        || acls[acl]?.internalIds?.contains(user.id) ?: false
                        || acls[acl]?.emails?.contains(user.email) ?: false
                }
        }
        .mapNotNull { it.role }
        .distinctBy { it.discordId }
        .toList()
}