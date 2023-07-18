package hu.kirdev.discordinator.messaging

import hu.kirdev.discordinator.config.asUser
import hu.kirdev.discordinator.config.asUserEntity
import hu.kirdev.discordinator.service.ServerService
import hu.kirdev.discordinator.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class TokenController(
    private val serverService: ServerService,
    private val userService: UserService,
    private val messageService: MessageService
) {

    private val log = LoggerFactory.getLogger(TokenController::class.java)

    @GetMapping("/tokens")
    fun tokens(model: Model, auth: Authentication): String {
        model.addAttribute("menu", "tokens")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        val user = auth.asUserEntity(userService)
        if (!user.admin)
            return "redirect:/403"
        model.addAttribute("tokens", messageService.getTokens())

        return "tokens"
    }

    @GetMapping("/tokens/{tokenId}")
    fun token(model: Model, auth: Authentication, @PathVariable tokenId: Int): String {
        model.addAttribute("menu", "tokens")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        val user = auth.asUserEntity(userService)
        if (!user.admin)
            return "redirect:/403"
        model.addAttribute("token", messageService.getTokenById(tokenId))

        return "token"
    }

    @PostMapping("/tokens")
    fun createToken(
        model: Model,
        auth: Authentication,
        @RequestParam tokenData: Map<String, String>,
    ): String {
        val user = auth.asUserEntity(userService)
        if (!user.admin)
            return "redirect:/403"

        messageService.createToken(tokenData)
        log.info("User {} created a token {}", user.fullName, tokenData)

        return "redirect:/tokens"
    }

    @PostMapping("/tokens/{tokenId}")
    fun changeToken(
        model: Model,
        auth: Authentication,
        @PathVariable tokenId: Int,
        @RequestParam ruleData: Map<String, String>,
    ): String {
        val user = auth.asUserEntity(userService)
        if (!user.admin)
            return "redirect:/403"

        messageService.changeToken(ruleData)
        log.info("User {} changed a token {} (id:{})", user.fullName, ruleData, tokenId)

        return "redirect:/tokens"
    }

}