package net.draycia.carbon.common.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.context.CommandContext;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.api.CarbonChatProvider;
import net.draycia.carbon.api.channels.TextChannel;
import net.draycia.carbon.api.commands.settings.CommandSettings;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.draycia.carbon.common.commands.arguments.ChannelArgument;
import net.kyori.adventure.identity.Identity;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ChannelCommand {

  private @NonNull final CarbonChat carbonChat;

  public ChannelCommand(@NonNull final CommandManager<CarbonUser> commandManager) {
    this.carbonChat = CarbonChatProvider.carbonChat();

    final CommandSettings commandSettings = this.carbonChat.commandSettings().get("channel");

    if (!commandSettings.enabled()) {
      return;
    }

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.channel")
        .argument(ChannelArgument.requiredChannelArgument())
        .handler(this::channel)
        .build()
    );

    commandManager.command(
      commandManager.commandBuilder(commandSettings.name(), commandSettings.aliases(),
        commandManager.createDefaultCommandMeta())
        .senderType(PlayerUser.class) // player
        .permission("carbonchat.channel.message")
        .argument(ChannelArgument.requiredChannelArgument())
        .argument(StringArgument.of("message")) // carbonchat.channel.message
        .handler(this::sendMessage)
        .build()
    );
  }

  private void channel(@NonNull final CommandContext<CarbonUser> context) {
    final PlayerUser user = (PlayerUser) context.getSender();
    final TextChannel channel = context.get("channel");

    if (user.channelSettings(channel).ignored()) {
      user.sendMessage(Identity.nil(), this.carbonChat.messageProcessor().processMessage(channel.cannotUseMessage(),
        "color", "<" + channel.channelColor(user).toString() + ">",
        "channel", channel.name()));

      return;
    }

    user.selectedChannel(channel);
  }

  private void sendMessage(@NonNull final CommandContext<CarbonUser> context) {
    final ChatChannel channel = context.get("channel");
    final String message = context.get("message");

    channel.sendComponentsAndLog(channel.parseMessage((PlayerUser) context.getSender(),
      message, false));
  }

}
