package hu.kirdev.discordinator.service

import discord4j.core.GatewayDiscordClient
import hu.kirdev.discordinator.event.addRolesForUser
import hu.kirdev.discordinator.event.resolveRoles
import hu.kirdev.discordinator.model.ServerEntity
import hu.kirdev.discordinator.model.UserEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
open class RoleService(
        private val client: GatewayDiscordClient,
        private val serverService: ServerService
) {

    @Async
    open fun addRoles(user: UserEntity, server: ServerEntity) {
        val lists = serverService.getAcls(server.id)
        val rules = serverService.getRules(server)
        val roles = resolveRoles(rules, user, lists)
        addRolesForUser(client, server, roles, user).block()
    }

}