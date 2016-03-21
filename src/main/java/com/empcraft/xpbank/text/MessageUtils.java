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

  /**
   * Null safe implementation of colorise method.
   * @param mystring the string to be colorized with ampersand sign.
   * @return a string, empty on null input, colorized on text input. But never null.
   */
  public static String colorise(String mystring) {
    if (null == mystring) {
      return "";
    }

    return ChatColor.translateAlternateColorCodes('&', mystring);
  }

  /**
   * Send a message to the specified player, if it is not empty.
   * @param player The bukkit player to send the message to.
   * @param text The text you'd like to send. Being colorized.
   */
  public static void sendMessageToPlayer(final Player player, final String text) {
    if (text == null || "".equals(text)) {
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
