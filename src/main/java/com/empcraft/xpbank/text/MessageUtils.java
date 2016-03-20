package com.empcraft.xpbank.text;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 * Text formatter.
 */
public final class MessageUtils {
  /**
   * Hidden private utiltily constructor.
   */
  private MessageUtils() { }

  public static String colorise(String mystring) {
    return ChatColor.translateAlternateColorCodes('&', mystring);
  }

  /**
   * Send a message to the specified player, if it is not empty.
   * @param player The bukkit player to send the message to.
   * @param text The text you'd like to send. Being colorized.
   */
  public static void sendMessageToPlayer(final Player player, final String text) {
    if ("".equals(text)) {
      return;
    }

    if (player == null) {
      return;
    }

    player.sendMessage(MessageUtils.colorise(text));
  }

  /**
   * Send a message to all/console, if it is not empty.
   * @param server the bukkit server.
   * @param text The text you'd like to send. Being colorized.
   */
  public static void sendMessageToAll(final Server server, final String text) {
    if ("".equals(text)) {
      return;
    }

    if (server == null) {
      return;
    }

    server.getConsoleSender().sendMessage(MessageUtils.colorise(text));
  }

}
