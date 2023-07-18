package hu.kirdev.discordinator.messaging

data class SimpleMessageDto(
    val token: String,
    val target: List<String>,
    var message: String,
)