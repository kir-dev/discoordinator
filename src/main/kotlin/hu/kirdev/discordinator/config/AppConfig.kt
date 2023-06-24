package hu.kirdev.discordinator.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "discoordinator")
data class AppConfig @ConstructorBinding constructor(
    val token: String,
    val baseUrl: String,
    val sysAdmins: List<String>,
    val inviteUrl: String
)