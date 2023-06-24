package hu.kirdev.discordinator.model

import hu.kirdev.discordinator.authsch.PersonEntitlement
import hu.kirdev.discordinator.converter.PersonEntitlementListConverter
import hu.kirdev.discordinator.converter.StringListConverter
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class UserEntity(
        @Id
        var id: String = "",

        @Column(nullable = false)
        var fullName: String = "",

        @Column(nullable = false)
        var nickname: String = "",

        @Column(nullable = false)
        var email: String = "",

        @Lob
        @Column(nullable = false)
        @Convert(converter = PersonEntitlementListConverter::class)
        var pekGroups: MutableList<PersonEntitlement> = mutableListOf(),

        @Column(nullable = false)
        var discordUsername: String = "",

        @Column(nullable = false)
        var avatarUrl: String = "",

        @Column(nullable = false)
        var discordId: Long = 0,

        @Column(nullable = false)
        var admin: Boolean = false,

)