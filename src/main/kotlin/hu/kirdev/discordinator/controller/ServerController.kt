package hu.kirdev.discordinator.controller

import hu.kirdev.discordinator.config.AppConfig
import hu.kirdev.discordinator.config.Lang
import hu.kirdev.discordinator.config.asUser
import hu.kirdev.discordinator.config.asUserEntity
import hu.kirdev.discordinator.service.ServerService
import hu.kirdev.discordinator.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import kotlin.jvm.optionals.getOrNull

@Controller
class ServerController(
        private val serverService: ServerService,
        private val userService: UserService,
        private val config: AppConfig
) {

    private val log = LoggerFactory.getLogger(ServerController::class.java)

    @GetMapping("/new-server")
    fun newServer(model: Model, auth: Authentication): String {
        model.addAttribute("menu", "new-server")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))
        model.addAttribute("inviteUrl", config.inviteUrl)

        return "newServer"
    }

    @GetMapping("/new-server-steps")
    fun newServerSteps(model: Model, auth: Authentication): String {
        model.addAttribute("menu", "new-server-steps")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        val code = serverService.createServer(auth.asUserEntity(userService), true, Lang.defaultReactMessage)
        model.addAttribute("code", code)
        return "newServerSteps"
    }

    @GetMapping("/server/{serverId}")
    fun index(model: Model, auth: Authentication, @PathVariable serverId: Int): String {
        model.addAttribute("menu", "server")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        val server = serverService.getServerById(serverId).getOrNull()
                ?: return "redirect:/403"
        val user = auth.asUserEntity(userService)
        if (server.ownerId != user.id)
            return "redirect:/403"
        model.addAttribute("server", server)

        return "server"
    }

    data class ServerChangeData(
            var renameUsers: Boolean = false,
            var reactMessage: String = ""
    )

    @PostMapping("/server/{serverId}/change")
    fun changeServerSettings(
            model: Model,
            auth: Authentication,
            @PathVariable serverId: Int,
            @ModelAttribute changeSettingsData: ServerChangeData,
    ): String {
        val server = serverService.getServerById(serverId).getOrNull()
                ?: return "redirect:/403"
        val user = auth.asUserEntity(userService)
        if (server.ownerId != user.id)
            return "redirect:/403"
        model.addAttribute("server", server)

        serverService.changeServer(server,
                changeSettingsData.renameUsers,
                changeSettingsData.reactMessage
        )
        log.info("User {} at {} changed the settings {}", user.fullName, serverId, changeSettingsData)

        return "redirect:/server/${server.id}"
    }

    @GetMapping("/server/{serverId}/lists")
    fun lists(model: Model, auth: Authentication, @PathVariable serverId: Int): String {
        model.addAttribute("menu", "lists")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        val server = serverService.getServerById(serverId).getOrNull()
                ?: return "redirect:/403"
        val user = auth.asUserEntity(userService)
        if (server.ownerId != user.id)
            return "redirect:/403"
        model.addAttribute("server", server)
        model.addAttribute("lists", serverService.getLists(server.id))

        return "lists"
    }

    @GetMapping("/server/{serverId}/lists/{listId}")
    fun list(
            model: Model,
            auth: Authentication,
            @PathVariable serverId: Int,
            @PathVariable listId: Int
    ): String {
        model.addAttribute("menu", "list")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        val server = serverService.getServerById(serverId).getOrNull()
                ?: return "redirect:/403"
        val user = auth.asUserEntity(userService)
        if (server.ownerId != user.id)
            return "redirect:/403"
        model.addAttribute("server", server)

        val list = serverService.getList(listId).getOrNull()
                ?: return "redirect:/403"
        if (list.serverId != server.id)
            return "redirect:/403"
        model.addAttribute("list", list)

        return "list"
    }

    data class ListCreateData(
            var name: String = "",
            var emails: String = "",
            var internalIds: String = "",
            var discordNames: String = ""
    )

    @PostMapping("/server/{serverId}/lists")
    fun createList(
            model: Model,
            auth: Authentication,
            @PathVariable serverId: Int,
            @ModelAttribute listCreateData: ListCreateData,
    ): String {
        val server = serverService.getServerById(serverId).getOrNull()
                ?: return "redirect:/403"
        val user = auth.asUserEntity(userService)
        if (server.ownerId != user.id)
            return "redirect:/403"
        model.addAttribute("server", server)

        serverService.createList(serverId, user, listCreateData)
        log.info("User {} at {} created a list {}", user.fullName, serverId, listCreateData)

        return "redirect:/server/${server.id}/lists"
    }

    @PostMapping("/server/{serverId}/lists/{listId}")
    fun changeList(
            model: Model,
            auth: Authentication,
            @PathVariable serverId: Int,
            @PathVariable listId: Int,
            @ModelAttribute listChangeData: ListCreateData,
    ): String {
        val server = serverService.getServerById(serverId).getOrNull()
                ?: return "redirect:/403"
        val user = auth.asUserEntity(userService)
        if (server.ownerId != user.id)
            return "redirect:/403"
        model.addAttribute("server", server)

        val list = serverService.getList(listId).getOrNull()
                ?: return "redirect:/403"
        if (list.serverId != server.id)
            return "redirect:/403"
        serverService.changeList(serverId, list, user, listChangeData)
        log.info("User {} at {} changed a list {} (id:{})", user.fullName, serverId, listChangeData, listId)

        return "redirect:/server/${server.id}/lists"
    }

    @GetMapping("/server/{serverId}/roles")
    fun roles(model: Model, auth: Authentication, @PathVariable serverId: Int): String {
        model.addAttribute("menu", "roles")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        val server = serverService.getServerById(serverId).getOrNull()
                ?: return "redirect:/403"
        val user = auth.asUserEntity(userService)
        if (server.ownerId != user.id)
            return "redirect:/403"
        model.addAttribute("server", server)
        model.addAttribute("roles", server.roles)

        return "roles"
    }

    @GetMapping("/server/{serverId}/rules")
    fun rules(model: Model, auth: Authentication, @PathVariable serverId: Int): String {
        model.addAttribute("menu", "rules")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        val server = serverService.getServerById(serverId).getOrNull()
                ?: return "redirect:/403"
        val user = auth.asUserEntity(userService)
        if (server.ownerId != user.id)
            return "redirect:/403"
        model.addAttribute("server", server)
        model.addAttribute("roles", server.roles)
        model.addAttribute("lists", serverService.getLists(server.id))
        model.addAttribute("rules", serverService.getRules(server))

        return "rules"
    }

    @GetMapping("/server/{serverId}/rules/{ruleId}")
    fun rule(model: Model, auth: Authentication, @PathVariable serverId: Int, @PathVariable ruleId: Int): String {
        model.addAttribute("menu", "rules")
        model.addAttribute("servers", serverService.getOwnedServers(auth.asUser().id))

        val server = serverService.getServerById(serverId).getOrNull()
                ?: return "redirect:/403"
        val user = auth.asUserEntity(userService)
        if (server.ownerId != user.id)
            return "redirect:/403"
        model.addAttribute("server", server)
        model.addAttribute("roles", server.roles)
        model.addAttribute("lists", serverService.getLists(server.id))
        model.addAttribute("rules", serverService.getRules(server))

        val rule = serverService.getRule(ruleId).getOrNull()
                ?: return "redirect:/403"
        if (rule.server?.id != server.id)
            return "redirect:/403"
        model.addAttribute("rule", rule)

        return "rule"
    }

    @PostMapping("/server/{serverId}/rules")
    fun createRule(
            model: Model,
            auth: Authentication,
            @PathVariable serverId: Int,
            @RequestParam ruleData: Map<String, String>,
    ): String {
        val server = serverService.getServerById(serverId).getOrNull()
                ?: return "redirect:/403"
        val user = auth.asUserEntity(userService)
        if (server.ownerId != user.id)
            return "redirect:/403"
        model.addAttribute("server", server)

        serverService.createRule(server, user, ruleData)
        log.info("User {} at {} created a list {}", user.fullName, serverId, ruleData)

        return "redirect:/server/${server.id}/rules"
    }

    @PostMapping("/server/{serverId}/rules/{ruleId}")
    fun changeRule(
            model: Model,
            auth: Authentication,
            @PathVariable serverId: Int,
            @PathVariable ruleId: Int,
            @RequestParam ruleData: Map<String, String>,
    ): String {
        val server = serverService.getServerById(serverId).getOrNull()
                ?: return "redirect:/403"
        val user = auth.asUserEntity(userService)
        if (server.ownerId != user.id)
            return "redirect:/403"
        model.addAttribute("server", server)

        val rule = serverService.getRule(ruleId).getOrNull()
                ?: return "redirect:/403"
        if (rule.server?.id != server.id)
            return "redirect:/403"
        serverService.changeRule(server, rule, user, ruleData)
        log.info("User {} at {} changed a rule {} (id:{})", user.fullName, serverId, ruleData, ruleId)

        return "redirect:/server/${server.id}/rules"
    }

}