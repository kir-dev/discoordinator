package hu.kirdev.discordinator.controller

import discord4j.common.util.Snowflake
import hu.kirdev.discordinator.config.asUserEntity
import hu.kirdev.discordinator.service.RoleService
import hu.kirdev.discordinator.service.InviteService
import hu.kirdev.discordinator.service.ServerService
import hu.kirdev.discordinator.service.UserService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import kotlin.jvm.optionals.getOrNull

private val ATTRIBUTE_NAME_CODE = "${IdentifyController::class.qualifiedName}.code"

@Controller
class IdentifyController(
        private val userService: UserService,
        private val serverService: ServerService,
        private val inviteService: InviteService,
        private val roleService: RoleService
) {

    private val log = LoggerFactory.getLogger(IdentifyController::class.java)

    @GetMapping("/identify/{code}")
    fun identify(@PathVariable code: String, request: HttpServletRequest): String {
        request.session.setAttribute(ATTRIBUTE_NAME_CODE, code)
        return "redirect:/identify-authsch"
    }

    @GetMapping("/identify-authsch")
    fun identifyAuthsch(request: HttpServletRequest, auth: Authentication): String {
        return if (request.session.getAttribute(ATTRIBUTE_NAME_CODE) != null) {
            val (userId, userName, avatarUrl, serverId) = inviteService.popLinkForAssociation(
                    request.session.getAttribute(ATTRIBUTE_NAME_CODE) as String
            ) ?: return "redirect:/identified?error=null"

            val user = auth.asUserEntity(userService)
            if (userService.identify(user, userId, userName, avatarUrl)) {
                log.info("User {} got identified as {}", user.fullName, userName)
                serverService.getServer(Snowflake.of(serverId)).getOrNull()?.let {
                    server -> roleService.addRoles(user, server)
                } ?: return "redirect:/identified?note=no-server"
                "redirect:/identified"
            } else {
                "redirect:/identified?error=already"
            }
        } else {
            "redirect:/profile"
        }
    }

    @GetMapping("/identified")
    fun identified(
            request: HttpServletRequest,
            @RequestParam(defaultValue = "") error: String,
            model: Model,
            auth: Authentication
    ): String {
        request.session.removeAttribute(ATTRIBUTE_NAME_CODE)
        model.addAttribute("error", error)
        model.addAttribute("user", auth.asUserEntity(userService))
        return "identified"
    }

}