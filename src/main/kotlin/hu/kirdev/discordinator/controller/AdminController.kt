package hu.kirdev.discordinator.controller

import hu.kirdev.discordinator.model.UserEntity
import hu.kirdev.discordinator.service.ServerService
import hu.kirdev.discordinator.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/")
class AdminController(
        private val userService: UserService,
        private val serverService: ServerService
) {

    @GetMapping("/users")
    fun users() = userService.getAll()

    @GetMapping("/servers")
    fun servers() = serverService.getAllServers()

    @GetMapping("/rules")
    fun rules() = serverService.getAllRules()

    @GetMapping("/roles")
    fun roles() = serverService.getAllRoles()

    @GetMapping("/lists")
    fun lists() = serverService.getAllLists()

}