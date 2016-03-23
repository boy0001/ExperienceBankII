package com.empcraft.xpbank.logic;

import code.husky.DatabaseConnectorException;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.JSONUtil;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.threads.SingleSignUpdateThread;
import com.empcraft.xpbank.threads.UpdateAllSignsThread;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

public final class SignHelper {

  /**
   * Utility class.
   */
  private SignHelper() {
  }

  /**
   * Updates a specific sign if a player is near.
   *
   * @param player
   *          the player who must be near.
   * @param sign
   *          the Sign to be updated.
   * @param expBankConfig
   *          The plugin config.
   */
  public static void updateSign(Player player, Sign sign, final ExpBankConfig expBankConfig) {
    SingleSignUpdateThread singleSignUpdateThread = new SingleSignUpdateThread(player, sign,
        expBankConfig);
    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(expBankConfig.getPlugin(),
        singleSignUpdateThread, 10L);
  }

  public static boolean isExperienceBankSignBlock(final Block block, final ExpBankConfig config) {
    boolean expBankSign = false;

    if (block.getType() != Material.SIGN_POST && block.getType() != Material.WALL_SIGN) {
      // listen only for signs.
      return expBankSign;
    }

    Sign sign = (Sign) block.getState();

    String firstLine = sign.getLines()[0];
    String coloredExpSign = MessageUtils.colorise(config.getExperienceBankActivationString());

    if (coloredExpSign.equals(firstLine)) {
      expBankSign = true;
    }

    return expBankSign;
  }

  public static String renderSignLines(String unrenderedLine, Player player,
      int storedPlayerExperience) {
    int playerCurrentXp = player.getTotalExperience();
    int levelsInBank = ExperienceLevelCalculator.getLevel(storedPlayerExperience);
    int levelafterWithdraw = ExperienceLevelCalculator
        .getLevel(storedPlayerExperience + playerCurrentXp);
    int leveldelta = levelafterWithdraw - player.getLevel();

    String renderedLine = unrenderedLine
        .replace(MessageUtils.MAGIC_KEYWORD_PLAYERNAME, player.getName())
        .replace(MessageUtils.MAGIC_KEYWORD_STORED_XP, Integer.toString(storedPlayerExperience))
        .replace(MessageUtils.MAGIC_KEYWORD_CURRENT_XP, Integer.toString(playerCurrentXp))
        .replace(MessageUtils.MAGIC_KEYWORD_CURRENT_LVL, Integer.toString(player.getLevel()))
        .replace(MessageUtils.MAGIC_KEYWORD_LEVELS_IN_BANK, Integer.toString(levelsInBank))
        .replace(MessageUtils.MAGIC_KEYWORD_LEVELS_GAIN_WITHDRAW, Integer.toString(leveldelta));

    return MessageUtils.colorise(renderedLine);
  }

  public static String[] getSignText(String[] lines, Player player,
      final ExpBankConfig expBankConfig) {
    int storedPlayerExperience = 0;
    List<String> signLines = expBankConfig.getSignContent();

    if (!MessageUtils.colorise(expBankConfig.getExperienceBankActivationString())
        .equals(lines[0])) {
      // this is not an ExpBank-Sign.
      return lines;
    }

    try {
      DataHelper dh = new DataHelper(null, expBankConfig);
      storedPlayerExperience = dh.getSavedExperience(player);
    } catch (DatabaseConnectorException confEx) {
      expBankConfig.getLogger().log(Level.WARNING,
          "Could not load experience for player [" + player.getName() + "].", confEx);
    }

    for (int line = 0; line < 4; line++) {
      String evaluatedLine = renderSignLines(signLines.get(line), player, storedPlayerExperience);
      lines[line] = JSONUtil.toJSON(evaluatedLine);
    }

    return lines;
  }

  /**
   * Updates all Chunks around the location.
   *
   * @param player
   *          the player at the location which must be online.
   * @param location
   *          the location to update including nearby chunks.
   */
  public static void scheduleUpdate(final Player player, final Location location,
      final ExpBankConfig expBankConfig) {
    // manual update.
    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(expBankConfig.getPlugin(),
        new UpdateAllSignsThread(player, location, expBankConfig), 5L);
  }
}
