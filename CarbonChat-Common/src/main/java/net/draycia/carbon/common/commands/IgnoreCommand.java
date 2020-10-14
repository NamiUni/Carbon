package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.commands.arguments.PlayerUserArgument;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

public class IgnoreCommand {

  private @NonNull final CarbonChat carbonChat;

  public IgnoreCommand(@NonNull final CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("ignore");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.ignore")
        .argument(PlayerUserArgument.requiredPlayerUserArgument())
        .handler(this::ignoreUser)
        .build()
    );
  }

  private void ignoreUser(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser sender = (PlayerUser) context.getSender();
    final PlayerUser targetUser = context.get("user");

    if (sender.ignoringUser(targetUser)) {
      sender.ignoringUser(targetUser, false);

      sender.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(
        this.carbonChat.translations().notIgnoringUser(), "player", targetUser.displayName()));
    } else {
      // TODO: schedule task because sync permission checks
      final String format;

      if (sender.hasPermission("carbonchat.ignore.exempt")) {
        format = this.carbonChat.translations().ignoreExempt();
      } else {
        sender.ignoringUser(targetUser, true);
        format = this.carbonChat.translations().ignoringUser();
      }

      final Component message = this.carbonChat.messageProcessor().processMessage(format,
        "sender", sender.displayName(), "player", targetUser.displayName());

      sender.sendMessage(Identity.nil(), message);
    }
  }

}
