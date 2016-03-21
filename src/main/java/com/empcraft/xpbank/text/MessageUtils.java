package com.empcraft.xpbank.text;

import com.empcraft.xpbank.ExpBank;
import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.ExperienceManager;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * Text formatter.
 */
public final class MessageUtils {
  /**
   * The levels the player would actually gain (level gain is not linear).
   */
  public static final String MAGIC_KEYWORD_LEVELS_GAIN_WITHDRAW = "{lvlbank2}";

  /**
   * Replaced with the levels the bank has in experience points.
   */
  public static final String MAGIC_KEYWORD_LEVELS_IN_BANK = "{lvlbank}";

  /**
   * Replaced with the players current level.
   */
  public static final String MAGIC_KEYWORD_CURRENT_LVL = "{lvl}";

  /**
   * This keyword will be replaced by the player's current xp.
   */
  public static final String MAGIC_KEYWORD_CURRENT_XP = "{exp}";

  /**
   * Will be replaced by the amount of XP the player has stored.
   */
  public static final String MAGIC_KEYWORD_STORED_XP = "{expbank}";

  /**
   * The text which will be replaced with the player's name.
   */
  public static final String MAGIC_KEYWORD_PLAYERNAME = "{player}";

  /**
   * Hidden private utiltily constructor.
   */
  private MessageUtils() {
  }

  /**
   * Null safe implementation of colorise method.
   *
   * @param mystring
   *          the string to be colorized with ampersand sign.
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
   *
   * @param player
   *          The bukkit player to send the message to.
   * @param text
   *          The text you'd like to send. Being colorized.
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
   *
   * @param server
   *          the bukkit server.
   * @param text
   *          The text you'd like to send. Being colorized.
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

  public static String evaluate(String mystring, Player player, int storedPlayerExperience) {
    ExperienceManager expMan = new ExperienceManager(player);

    if (mystring.contains(MAGIC_KEYWORD_PLAYERNAME)) {
      mystring = mystring.replace(MAGIC_KEYWORD_PLAYERNAME, player.getName());
    }

    if (mystring.contains(MAGIC_KEYWORD_STORED_XP)) {
      mystring = mystring.replace(MAGIC_KEYWORD_STORED_XP,
          Integer.toString(storedPlayerExperience));
    }

    if (mystring.contains(MAGIC_KEYWORD_CURRENT_XP)) {
      mystring = mystring.replace(MAGIC_KEYWORD_CURRENT_XP,
          Integer.toString(expMan.getCurrentExp()));
    }

    if (mystring.contains(MAGIC_KEYWORD_CURRENT_LVL)) {
      mystring = mystring.replace(MAGIC_KEYWORD_CURRENT_LVL, Integer.toString(player.getLevel()));
    }

    if (mystring.contains(MAGIC_KEYWORD_LEVELS_IN_BANK)) {
      mystring = mystring.replace(MAGIC_KEYWORD_LEVELS_IN_BANK,
          Integer.toString(expMan.getLevelForExp(storedPlayerExperience)));
    }

    if (mystring.contains(MAGIC_KEYWORD_LEVELS_GAIN_WITHDRAW)) {
      mystring = mystring.replace(MAGIC_KEYWORD_LEVELS_GAIN_WITHDRAW,
          Integer.toString((expMan.getLevelForExp(expMan.getCurrentExp() + storedPlayerExperience)
              - player.getLevel())));
    }

    return colorise(mystring);
  }

  public static String[] getSignText(String[] lines, Player player, Sign sign,
      final ExpBankConfig expBankConfig, final ExpBank exp) {
    String[] signLines = expBankConfig.getSignContent();

    if (lines[0].equals(MessageUtils.colorise(expBankConfig.getExperienceBankActivationString()))) {
      int storedPlayerExperience = exp.getExp(player.getUniqueId());
      lines[0] = evaluate(signLines[0], player, storedPlayerExperience);
      lines[1] = evaluate(signLines[1], player, storedPlayerExperience);
      lines[2] = evaluate(signLines[2], player, storedPlayerExperience);
      lines[3] = evaluate(signLines[3], player, storedPlayerExperience);
    }

    return lines;
  }
}
