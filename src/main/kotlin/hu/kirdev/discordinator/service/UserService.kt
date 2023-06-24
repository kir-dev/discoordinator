package hu.kirdev.discordinator.service

import discord4j.common.util.Snowflake
import hu.kirdev.discordinator.authsch.ProfileResponse
import hu.kirdev.discordinator.config.AppConfig
import hu.kirdev.discordinator.model.UserEntity
import hu.kirdev.discordinator.repo.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
open class UserService(
        private val userRepository: UserRepository,
        private val config: AppConfig
) {

    @Transactional(readOnly = false, isolation = Isolation.REPEATABLE_READ)
    open fun getOrCreateUser(profile: ProfileResponse): UserEntity {
        val user = userRepository.findById(profile.internalId).getOrNull()
                ?: userRepository.save(UserEntity(
                        id              = profile.internalId,
                        fullName        = "${profile.surname} ${profile.givenName}",
                        nickname        = "",
                        email           = profile.email ?: "",
                        pekGroups       = profile.eduPersonEntitlement?.toMutableList() ?: mutableListOf(),
                        discordUsername = "",
                        discordId       = 0,
                        admin           = config.sysAdmins.contains(profile.internalId)
                ))

        user.email = profile.email ?: user.email
        user.pekGroups = profile.eduPersonEntitlement?.toMutableList() ?: mutableListOf()
        userRepository.save(user)
        return user
    }

    @Transactional(readOnly = true)
    open fun getUser(id: String): UserEntity {
        return userRepository.findById(id).orElseThrow()
    }

    @Transactional(readOnly = true)
    open fun getUserByDiscordId(id: Long): UserEntity {
        return userRepository.findByDiscordId(id).orElseThrow()
    }

    @Transactional(readOnly = false, isolation = Isolation.REPEATABLE_READ)
    open fun identify(user: UserEntity, userId: Snowflake, userName: String, avatarUrl: String): Boolean {
        if (user.discordId == 0L) {
            user.discordId = userId.asLong()
            user.discordUsername = userName
            user.avatarUrl = avatarUrl
            userRepository.save(user)
            return true
        }
        return false
    }

    @Transactional(readOnly = true)
    open fun isUserLinked(id: Snowflake): Boolean {
        return userRepository.findByDiscordId(id.asLong()).isPresent
    }

    @Transactional(readOnly = false)
    open fun changeNickname(nickname: String, user: UserEntity) {
        user.nickname = nickname.replace("[^a-zA-Z0-9áéíóöőúüűÁÉÍÓÖŐÚÜŰ \\-]".toRegex(), "")
                .trim()
                .take(16)
        userRepository.save(user)
    }

    @Transactional(readOnly = true)
    open fun getAll(): List<UserEntity> {
        return userRepository.findAll().toList()
    }

}