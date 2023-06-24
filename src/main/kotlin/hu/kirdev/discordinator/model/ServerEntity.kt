package hu.kirdev.discordinator.model

import jakarta.persistence.*

@Entity
@Table(name = "servers")
data class ServerEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Int = 0,

        @Column(nullable = false)
        var name: String = "",

        @Column(nullable = false)
        var discordId: Long = 0,

        @Column(nullable = false)
        var ownerId: String = "",

        @Column(nullable = false)
        var renameUsers: Boolean = false,

        @Column(nullable = false)
        var logo: String = "",

        @OneToMany(fetch = FetchType.LAZY)
        @JoinColumn(name = "serverId")
        var roles: MutableList<RoleEntity> = mutableListOf(),

        @Column(nullable = false)
        var usesNeptunCodes: Boolean = false,

        @Column(nullable = false)
        var reactMessage: String = "",

)
