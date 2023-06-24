package hu.kirdev.discordinator.repo

import hu.kirdev.discordinator.model.*
import org.springframework.data.repository.CrudRepository
import java.util.*

interface ListRepository : CrudRepository<ListEntity, Int> {
    fun findAllByServerId(serverId: Int): List<ListEntity>
}

interface RoleRepository : CrudRepository<RoleEntity, Long>

interface RuleRepository : CrudRepository<RuleEntity, Int> {
    fun findAllByServer(server: ServerEntity): List<RuleEntity>
}

interface ServerRepository : CrudRepository<ServerEntity, Int> {
    fun countByDiscordId(discordId: Long): Int
    fun findByDiscordId(discordId: Long): Optional<ServerEntity>
    fun findAllByOwnerId(ownerId: String): List<ServerEntity>
}

interface UserRepository : CrudRepository<UserEntity, String> {
    fun findByDiscordId(discordId: Long): Optional<UserEntity>
}