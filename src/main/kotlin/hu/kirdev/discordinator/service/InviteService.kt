package hu.kirdev.discordinator.service

import discord4j.common.util.Snowflake
import hu.kirdev.discordinator.model.ServerEntity
import org.springframework.stereotype.Service

data class ServerInfo(
        val userId: Snowflake,
        val name: String,
        val avatar: String,
        val serverId: Long
)

@Service
class InviteService {

    private val codeToDiscordUserMappings: MutableMap<String, ServerInfo> = mutableMapOf()
    private val codeToDiscordServerMapping: MutableMap<String, ServerEntity> = mutableMapOf()

    fun createLinkForSetup(code: String, server: ServerEntity) = codeToDiscordServerMapping.put(code, server)

    fun popLinkForSetup(code: String) = codeToDiscordServerMapping.remove(code)

    fun createLinkForAssociation(
            code: String,
            userId: Snowflake,
            name: String,
            avatar: String,
            serverId: Long
    ): String {
        codeToDiscordUserMappings[code] = ServerInfo(userId, name, avatar, serverId)
        return code
    }

    fun popLinkForAssociation(code: String) = codeToDiscordUserMappings.remove(code)

}