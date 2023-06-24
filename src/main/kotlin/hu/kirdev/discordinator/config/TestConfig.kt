package hu.kirdev.discordinator.config

import hu.kirdev.discordinator.model.UserEntity
import hu.kirdev.discordinator.service.ServerService
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("test")
@Configuration
class TestConfig(
    val serverService: ServerService
) {

    @PostConstruct
    fun init() {
        val code = serverService.createServer(UserEntity(id = "asdsadsadsa"), true, Lang.defaultReactMessage)
        println(code)
    }

}