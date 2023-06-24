package hu.kirdev.discordinator.config

import hu.kirdev.discordinator.service.UserService
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import java.io.Serializable
import java.security.Principal

private const val ID_ATTRIBUTE = "id"

class DiscoordinatorUser(
        val id: String,
        authorities: List<GrantedAuthority>
) : DefaultOAuth2User(authorities, mapOf(ID_ATTRIBUTE to id), ID_ATTRIBUTE), Principal, Serializable {

    override fun getName() = id

}

fun Authentication.asUser() = this.principal as DiscoordinatorUser

fun Authentication.asUserEntity(userService: UserService) = userService.getUser(this.asUser().id)