package hu.kirdev.discordinator.messaging

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/message")
class MessageProxyController(
    private val messageService: MessageService
) {

    private val log = LoggerFactory.getLogger(MessageProxyController::class.java)

    @CrossOrigin(origins = ["*"])
    @PostMapping("/simple")
    fun simple(@RequestBody request: SimpleMessageDto): MessageResponse {
        val token = messageService.getToken(request.token)
            ?: return MessageResponse(false, "Invalid token", listOf())

        log.info("Distributing simple message: '{}' for: {}", request.message, request.target.joinToString(", "))
        val delivered = request.target.filter {
            messageService.sendMessageInternalId(it, request.message, token)
        }
        return MessageResponse(delivered.isNotEmpty(), null, delivered)
    }

}