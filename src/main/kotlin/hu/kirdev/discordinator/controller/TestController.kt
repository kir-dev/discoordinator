package hu.kirdev.discordinator.controller

import hu.kirdev.discordinator.repo.*
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Profile("test")
@RestController
@RequestMapping("/test")
class TestController(
        private val listRepository: ListRepository,
        private val roleRepository: RoleRepository,
        private val ruleRepository: RuleRepository,
        private val serverRepository: ServerRepository,
        private val userRepository: UserRepository,
) {

    @GetMapping("/servers")
    fun servers() = serverRepository.findAll()

    @GetMapping("/roles")
    fun roles() = roleRepository.findAll()

}