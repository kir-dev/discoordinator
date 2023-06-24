package hu.kirdev.discordinator.event

import discord4j.core.GatewayDiscordClient
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import discord4j.rest.util.Permission
import hu.kirdev.discordinator.config.Lang
import hu.kirdev.discordinator.service.InviteService
import hu.kirdev.discordinator.service.ServerService
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

private const val SYNC_SUBCOMMAND = "sync"
private const val SETUP_SUBCOMMAND = "setup"
private const val CODE_SUBCOMMAND_PARAMETER = "code"
private const val MESSAGE_SUBCOMMAND = "message"

private const val COMMAND_NAME = "discoordinator"

@Service
class AdminCommandListener(
    private val invites: InviteService,
    private val serverService: ServerService
) : EventListener<ApplicationCommandInteractionEvent> {

    final override val eventType: Class<ApplicationCommandInteractionEvent>
        get() = ApplicationCommandInteractionEvent::class.java

    override fun execute(event: ApplicationCommandInteractionEvent): Mono<Void> {

        if (event.commandName.equals(COMMAND_NAME, true)) {
            if (event.interaction.guildId.isEmpty) {
                return event.reply()
                    .withEphemeral(true)
                    .withContent(Lang.onlyAvailableForGuilds)
            }
            return Mono.just(event.interaction.member.get())
                .flatMap { it.basePermissions }
                .flatMap {
                    if (it.contains(Permission.ADMINISTRATOR)) {
                        return@flatMap when (getSubCommand(event)) {
                            SYNC_SUBCOMMAND    -> handleSync(event, serverService)
                            SETUP_SUBCOMMAND   -> handleSetup(event, getCode(event), invites, serverService)
                            MESSAGE_SUBCOMMAND -> handleMessage(event, serverService)
                            else               -> handleElse(event)
                        }
                    }
                    return@flatMap event.reply()
                        .withEphemeral(true)
                        .withContent(Lang.adminRoleRequired)
                }
                .then()
        }
        return Mono.empty()
    }

    private fun getSubCommand(event: ApplicationCommandInteractionEvent) =
            event.interaction.commandInteraction.get().options[0].name

    private fun getCode(event: ApplicationCommandInteractionEvent) =
            event.interaction.commandInteraction.get().options[0].options[0].value.get().asString()

    private fun handleElse(event: ApplicationCommandInteractionEvent) =
        event.reply()
            .withEphemeral(true)
            .withContent(Lang.invalidCommand)

    companion object {
        fun registerCommand(client: GatewayDiscordClient, applicationId: Long) {
            val commandRequest: ApplicationCommandRequest = ApplicationCommandRequest.builder()
                .name(COMMAND_NAME)
                .description(Lang.commandDescription)
                .addOption(ApplicationCommandOptionData.builder()
                    .name(SYNC_SUBCOMMAND)
                    .description(Lang.syncDescription)
                    .type(ApplicationCommandOption.Type.SUB_COMMAND.value)
                    .build())
                .addOption(ApplicationCommandOptionData.builder()
                    .name(SETUP_SUBCOMMAND)
                    .description(Lang.setupDescription)
                    .type(ApplicationCommandOption.Type.SUB_COMMAND.value)
                    .addOption(ApplicationCommandOptionData.builder()
                        .name(CODE_SUBCOMMAND_PARAMETER)
                        .description(Lang.setupCodeDescription)
                        .required(true)
                        .type(ApplicationCommandOption.Type.STRING.value)
                        .build())
                    .build())
                .addOption(ApplicationCommandOptionData.builder()
                    .name(MESSAGE_SUBCOMMAND)
                    .description(Lang.messageDescription)
                    .type(ApplicationCommandOption.Type.SUB_COMMAND.value)
                    .build())
                .build()

            client.restClient.applicationService
                .createGlobalApplicationCommand(applicationId, commandRequest)
                .subscribe()
        }
    }

}