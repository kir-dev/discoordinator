package hu.kirdev.discordinator.model

import hu.kirdev.discordinator.converter.IntListConverter
import jakarta.persistence.*

@Entity
@Table(name = "rules")
data class RuleEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Int = 0,

        @ManyToOne(fetch = FetchType.EAGER)
        var server: ServerEntity? = null,

        @Column(nullable = false)
        var name: String = "",

        @Column(nullable = false)
        var enabled: Boolean = false,

        @Column(nullable = false)
        var inPekGroup: String = "",

        @Column(nullable = false)
        var isActiveInPekGroup: Boolean = true,

        @Column(nullable = false)
        var hasPekRole: String = "",

        @Lob
        @Column(nullable = false)
        @Convert(converter = IntListConverter::class)
        var inAnyAcl: MutableList<Int> = mutableListOf(),

        @ManyToOne(fetch = FetchType.EAGER)
        var role: RoleEntity? = null,
)