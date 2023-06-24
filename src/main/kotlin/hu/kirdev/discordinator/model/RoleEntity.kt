package hu.kirdev.discordinator.model

import jakarta.persistence.*
import java.text.FieldPosition

@Entity
@Table(name = "roles")
data class RoleEntity(
        @Id
        var discordId: Long = 0,

        @Column(nullable = false)
        var name: String = "",

        @Column(nullable = false)
        var color: String = "",

        @Column(nullable = false)
        var position: Int = 0,
)
