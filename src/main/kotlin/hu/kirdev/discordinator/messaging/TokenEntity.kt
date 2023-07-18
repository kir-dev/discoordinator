package hu.kirdev.discordinator.messaging

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "tokens")
data class TokenEntity(
    @Id
    @Column(nullable = false)
    @GeneratedValue
    var id: Int = 0,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var token: String = "",
)