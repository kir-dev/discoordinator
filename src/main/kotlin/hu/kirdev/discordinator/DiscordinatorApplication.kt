package hu.kirdev.discordinator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class DiscordinatorApplication

const val APP_NAME = "discoordinator"

fun main(args: Array<String>) {
    runApplication<DiscordinatorApplication>(*args)
}
