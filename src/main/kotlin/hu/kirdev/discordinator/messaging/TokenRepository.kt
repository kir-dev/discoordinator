package hu.kirdev.discordinator.messaging

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface TokenRepository : CrudRepository<TokenEntity, Int> {

    fun findByToken(token: String): Optional<TokenEntity>

}