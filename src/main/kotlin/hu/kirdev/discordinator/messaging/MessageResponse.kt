package hu.kirdev.discordinator.messaging

data class MessageResponse(
    val succeed: Boolean,
    val message: String?,
    val delivered: List<String>
)