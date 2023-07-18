package hu.kirdev.discordinator.messaging

import discord4j.common.util.Snowflake
import discord4j.core.GatewayDiscordClient
import discord4j.core.`object`.entity.User
import discord4j.core.`object`.entity.channel.PrivateChannel
import hu.kirdev.discordinator.model.UserEntity
import hu.kirdev.discordinator.repo.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
open class MessageService(
    private val tokenRepository: TokenRepository,
    private val client: GatewayDiscordClient,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    open fun getToken(token: String): TokenEntity? {
        return tokenRepository.findByToken(token).getOrNull()
    }

    @Transactional(readOnly = true)
    open fun sendMessageInternalId(internalId: String, message: String, token: TokenEntity): Boolean {
        val userEntity = userRepository.findById(internalId).getOrNull()
            ?: return false

        client.getUserById(Snowflake.of(userEntity.discordId))
            .flatMap { user: User -> user.privateChannel }
            .flatMap { channel: PrivateChannel ->
                channel.createMessage("`${token.name}` $message")
            }
            .block()
        return true
    }

    @Transactional(readOnly = true)
    open fun getTokens(): List<TokenEntity> {
        return tokenRepository.findAll().toList()
    }

    @Transactional(readOnly = true)
    open fun getTokenById(id: Int): TokenEntity? {
        return tokenRepository.findById(id).getOrNull()
    }

    @Transactional(readOnly = false, isolation = Isolation.REPEATABLE_READ)
    open fun createToken(ruleData: Map<String, String>): Boolean {
        val token = TokenEntity(
            name = ruleData.getOrDefault("name", "").trim(),
            token = ruleData.getOrDefault("token", "").trim(),
        )

        tokenRepository.save(token)
        return true
    }

    @Transactional(readOnly = false, isolation = Isolation.REPEATABLE_READ)
    open fun changeToken(tokenData: Map<String, String>): Boolean {
        val token = tokenRepository.findById(tokenData.getOrDefault("id", "0").toInt()).getOrNull()
            ?: return false

        with(token) {
            this.name = tokenData.getOrDefault("name", "").trim()
            this.token = tokenData.getOrDefault("token", "").trim()
        }
        tokenRepository.save(token)
        return true
    }

}