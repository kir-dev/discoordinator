package hu.kirdev.discordinator.model

import hu.kirdev.discordinator.converter.StringListConverter
import jakarta.persistence.*

@Entity
@Table(name = "acls")
data class ListEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Int = 0,

        @Column(nullable = false)
        var name: String = "",

        @Column(nullable = false)
        var serverId: Int = 0,

        @Lob
        @Column(nullable = false)
        @Convert(converter = StringListConverter::class)
        var emails: MutableList<String> = mutableListOf(),

        @Lob
        @Column(nullable = false)
        @Convert(converter = StringListConverter::class)
        var internalIds: MutableList<String> = mutableListOf(),

        @Lob
        @Column(nullable = false)
        @Convert(converter = StringListConverter::class)
        var discordNames: MutableList<String> = mutableListOf(),

        @Lob
        @Column(nullable = false)
        @Convert(converter = StringListConverter::class)
        var neptunCodes: MutableList<String> = mutableListOf()
)
