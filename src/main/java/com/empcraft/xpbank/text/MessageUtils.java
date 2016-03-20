/**
 *
 */
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

  public static void sendMessageToPlayer(final Player player, final String text) {
    if ("".equals(text)) {
      return;
    }

    player.sendMessage(MessageUtils.colorise(text));
  }

  public static void sendMessageToAll(final Server server, final String text) {
    if ("".equals(text)) {
      return;
    }

    server.getConsoleSender().sendMessage(MessageUtils.colorise(text));
  }

}
