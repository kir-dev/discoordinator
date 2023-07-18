package hu.kirdev.discordinator.controller

import hu.kirdev.discordinator.config.asUser
import hu.kirdev.discordinator.config.asUserEntity
import hu.kirdev.discordinator.service.ServerService
import hu.kirdev.discordinator.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class CoreController(
        private val serverService: ServerService,
        private val userService: UserService
) {

    private val log = LoggerFactory.getLogger(CoreController::class.java)

    @GetMapping("/")
    fun index(): String {
        return "index"
    }

    @GetMapping("/403")
    fun noAccess(model: Model, auth: Authentication): String {
        model.addAttribute("menu", "403")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        return "403"
    }

    @ResponseBody
    @GetMapping("/api/version")
    fun version(model: Model, auth: Authentication): String {
        return "1.1.0"
    }

    @GetMapping("/start")
    fun start(model: Model, auth: Authentication): String {
        model.addAttribute("menu", "start")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        return "start"
    }

    @GetMapping("/profile")
    fun profile(model: Model, auth: Authentication): String {
        model.addAttribute("menu", "profile")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        model.addAttribute("user", auth.asUserEntity(userService))

        return "profile"
    }

    data class ChangeNicknameRequest(var nickname: String = "")

    @PostMapping("/profile/change")
    fun changeNickname(@ModelAttribute request: ChangeNicknameRequest, auth: Authentication): String {
        val user = auth.asUserEntity(userService)
        userService.changeNickname(request.nickname, user)
        log.info("User {} set their nickname to {}", user.fullName, request.nickname)
        return "redirect:/profile"
    }

}