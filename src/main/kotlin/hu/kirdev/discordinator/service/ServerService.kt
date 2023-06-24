package hu.kirdev.discordinator.service

import discord4j.common.util.Snowflake
import hu.kirdev.discordinator.config.Lang
import hu.kirdev.discordinator.controller.ServerController
import hu.kirdev.discordinator.model.*
import hu.kirdev.discordinator.repo.ListRepository
import hu.kirdev.discordinator.repo.RoleRepository
import hu.kirdev.discordinator.repo.RuleRepository
import hu.kirdev.discordinator.repo.ServerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
open class ServerService(
        private val serverRepository: ServerRepository,
        private val roleRepository: RoleRepository,
        private val listRepository: ListRepository,
        private val ruleRepository: RuleRepository,
        private val inviteService: InviteService,
        private val secureUuid: SecureUuid,
) {

    @Transactional(readOnly = false, isolation = Isolation.REPEATABLE_READ)
    open fun linkServer(
            server: ServerEntity,
            name: String,
            discordId: Long,
            logo: String,
            roles: List<RoleEntity>
    ): String {
        if (server.discordId != 0L)
            return Lang.serverIsAlreadyConnected

        if (serverRepository.countByDiscordId(discordId) > 0)
            return Lang.serverIsAlreadyConnectedToOther

        server.name = name
        server.discordId = discordId
        server.logo = logo
        server.roles.clear()
        server.roles.addAll(roles)
        roleRepository.saveAll(roles)
        serverRepository.save(server)

        return Lang.serverConnected
    }

    @Transactional(readOnly = false, isolation = Isolation.REPEATABLE_READ)
    open fun syncServer(name: String, discordId: Long, logo: String, roles: List<RoleEntity>): String {
        val server = serverRepository.findByDiscordId(discordId).getOrNull()
                ?: return Lang.notConnected

        server.name = name
        server.logo = logo
        server.roles.clear()
        server.roles.addAll(roles)
        roleRepository.saveAll(roles)
        serverRepository.save(server)

        return Lang.serverSynced
    }

    fun createServer(user: UserEntity, renameUsers: Boolean, reactMessage: String): String {
        val server = ServerEntity()
        server.ownerId = user.id
        server.renameUsers = renameUsers
        server.reactMessage = reactMessage
        server.usesNeptunCodes = false

        val code = secureUuid.generate()
        inviteService.createLinkForSetup(code, server)
        return code
    }

    @Transactional(readOnly = true)
    open fun getServer(id: Snowflake): Optional<ServerEntity> {
        return serverRepository.findByDiscordId(id.asLong())
    }

    @Transactional(readOnly = true)
    open fun getServerById(id: Int): Optional<ServerEntity> {
        return serverRepository.findById(id)
    }

    @Transactional(readOnly = true)
    open fun getOwnedServers(id: String): List<ServerEntity> {
        return serverRepository.findAllByOwnerId(id)
    }

    @Transactional(readOnly = true)
    open fun getList(listId: Int): Optional<ListEntity> {
        return listRepository.findById(listId)
    }

    @Transactional(readOnly = true)
    open fun getLists(id: Int): List<ListEntity> {
        return listRepository.findAllByServerId(id)
    }

    @Transactional(readOnly = false)
    open fun createList(serverId: Int, user: UserEntity, acl: ServerController.ListCreateData) {
        val list = ListEntity(
            name         = acl.name,
            serverId     = serverId,
            emails       = acl.emails.split('\n', '\r', ',')
                    .filter { it.isNotBlank() }.map { it.trim() }.toMutableList(),
            internalIds  = acl.internalIds.split('\n', '\r', ',')
                    .filter { it.isNotBlank() }.map { it.trim() }.toMutableList(),
            discordNames = acl.discordNames.split('\n', '\r', ',')
                    .filter { it.isNotBlank() }.map { it.trim() }.toMutableList()
        )

        listRepository.save(list)
    }

    @Transactional(readOnly = false, isolation = Isolation.REPEATABLE_READ)
    open fun changeList(serverId: Int, list: ListEntity, user: UserEntity, acl: ServerController.ListCreateData) {
        list.name         = acl.name
        list.emails       = acl.emails.split('\n', '\r', ',')
                .filter { it.isNotBlank() }.map { it.trim() }.toMutableList()
        list.internalIds  = acl.internalIds.split('\n', '\r', ',')
                .filter { it.isNotBlank() }.map { it.trim() }.toMutableList()
        list.discordNames = acl.discordNames.split('\n', '\r', ',')
                .filter { it.isNotBlank() }.map { it.trim() }.toMutableList()

        listRepository.save(list)
    }

    @Transactional(readOnly = true)
    open fun getRule(ruleId: Int): Optional<RuleEntity> {
        return ruleRepository.findById(ruleId)
    }

    @Transactional(readOnly = true)
    open fun getRules(server: ServerEntity): List<RuleEntity> {
        return ruleRepository.findAllByServer(server)
    }

    @Transactional(readOnly = false, isolation = Isolation.REPEATABLE_READ)
    open fun createRule(server: ServerEntity, user: UserEntity, ruleData: Map<String, String>): Boolean {
        val role = roleRepository.findById(ruleData.getOrDefault("role", "0").toLong()).getOrNull()
                ?: return false
        if (server.roles.none { it.discordId == role.discordId })
            return false

        val rule = RuleEntity(
                name = ruleData.getOrDefault("name", "").trim(),
                server = server,
                enabled = ruleData.getOrDefault("enabled", "off")
                        .equals("on", ignoreCase = true),
                inPekGroup = ruleData.getOrDefault("pekGroup", ""),
                isActiveInPekGroup = ruleData.getOrDefault("pekGroupActive", "off")
                        .equals("on", ignoreCase = true),
                hasPekRole = ruleData.getOrDefault("pekRole", ""),
                inAnyAcl = collectAcls(server, ruleData),
                role = role
        )

        ruleRepository.save(rule)
        return true
    }

    private fun collectAcls(server: ServerEntity, ruleData: Map<String, String>): MutableList<Int> {
        return listRepository.findAllByServerId(server.id)
                .filter { ruleData.containsKey("inList_${it.id}")
                        && ruleData["inList_${it.id}"].equals("on", ignoreCase = true) }
                .map { it.id }
                .toMutableList()
    }

    @Transactional(readOnly = false, isolation = Isolation.REPEATABLE_READ)
    open fun changeRule(
            server: ServerEntity,
            rule: RuleEntity,
            user: UserEntity,
            ruleData: Map<String, String>
    ): Boolean {
        val role = roleRepository.findById(ruleData.getOrDefault("role", "0").toLong()).getOrNull()
                ?: return false
        if (server.roles.none { it.discordId == role.discordId })
            return false

        with(rule) {
            name = ruleData.getOrDefault("name", "").trim()
            this.server = server
            enabled = ruleData.getOrDefault("enabled", "off")
                    .equals("on", ignoreCase = true)
            inPekGroup = ruleData.getOrDefault("pekGroup", "")
            isActiveInPekGroup = ruleData.getOrDefault("pekGroupActive", "off")
                    .equals("on", ignoreCase = true)
            hasPekRole = ruleData.getOrDefault("pekRole", "")
            inAnyAcl = collectAcls(server, ruleData)
            this.role = role
        }
        return true
    }

    @Transactional(readOnly = false, isolation = Isolation.REPEATABLE_READ)
    open fun changeServer(server: ServerEntity, renameUsers: Boolean, reactMessage: String) {
        server.renameUsers = renameUsers
        server.reactMessage = reactMessage
        serverRepository.save(server)
    }

    fun getAcls(serverId: Int): Map<Int, ListEntity> {
        return listRepository.findAllByServerId(serverId).associateBy { it.id }
    }

    @Transactional(readOnly = true)
    open fun getAllRoles(): List<RoleEntity> {
        return roleRepository.findAll().toList()
    }

    @Transactional(readOnly = true)
    open fun getAllRules(): List<RuleEntity> {
        return ruleRepository.findAll().toList()
    }

    @Transactional(readOnly = true)
    open fun getAllServers(): List<ServerEntity> {
        return serverRepository.findAll().toList()
    }

    @Transactional(readOnly = true)
    open fun getAllLists(): List<ListEntity> {
        return listRepository.findAll().toList()
    }

}