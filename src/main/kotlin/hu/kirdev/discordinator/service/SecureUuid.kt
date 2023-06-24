package hu.kirdev.discordinator.service

import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*

@Service
class SecureUuid {

    private val random = SecureRandom()

    fun generate(): String {
        return UUID(random.nextLong(), random.nextLong()).toString().replace("-", "")
    }

}