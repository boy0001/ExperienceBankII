package com.empcraft.xpbank.logic;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.JSONUtil;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.threads.SingleSignUpdateThread;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

public final class SignHelper {

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

  public static boolean isExperienceBankSign(final Sign sign, final ExpBankConfig config) {
    boolean expBankSign = false;

    String firstLine = sign.getLines()[0];
    String coloredExpSign = MessageUtils.colorise(config.getExperienceBankActivationString());

    if (coloredExpSign.equals(firstLine)) {
      expBankSign = true;
    }

    return expBankSign;
  }

  public static String renderSignLines(String mystring, Player player, int storedPlayerExperience) {
    int playerCurrentXp = player.getTotalExperience();
  
    if (mystring.contains(MessageUtils.MAGIC_KEYWORD_PLAYERNAME)) {
      mystring = mystring.replace(MessageUtils.MAGIC_KEYWORD_PLAYERNAME, player.getName());
    }
  
    if (mystring.contains(MessageUtils.MAGIC_KEYWORD_STORED_XP)) {
      mystring = mystring.replace(MessageUtils.MAGIC_KEYWORD_STORED_XP,
          Integer.toString(storedPlayerExperience));
    }
  
    if (mystring.contains(MessageUtils.MAGIC_KEYWORD_CURRENT_XP)) {
      mystring = mystring.replace(MessageUtils.MAGIC_KEYWORD_CURRENT_XP,
          Integer.toString(playerCurrentXp));
    }
  
    if (mystring.contains(MessageUtils.MAGIC_KEYWORD_CURRENT_LVL)) {
      mystring = mystring.replace(MessageUtils.MAGIC_KEYWORD_CURRENT_LVL, Integer.toString(player.getLevel()));
    }
  
    if (mystring.contains(MessageUtils.MAGIC_KEYWORD_LEVELS_IN_BANK)) {
      int levelsInBank = ExperienceLevelCalculator.getLevel(storedPlayerExperience);
      mystring = mystring.replace(MessageUtils.MAGIC_KEYWORD_LEVELS_IN_BANK,
          Integer.toString(levelsInBank));
    }
  
    if (mystring.contains(MessageUtils.MAGIC_KEYWORD_LEVELS_GAIN_WITHDRAW)) {
      int levelafterWithdraw = ExperienceLevelCalculator
          .getLevel(storedPlayerExperience + playerCurrentXp);
      int leveldelta = levelafterWithdraw - player.getLevel();
  
      mystring = mystring.replace(MessageUtils.MAGIC_KEYWORD_LEVELS_GAIN_WITHDRAW,
          Integer.toString(leveldelta));
    }
  
    return MessageUtils.colorise(mystring);
  }

  public static String[] getSignText(String[] lines, Player player, Sign sign,
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
    } catch (ConfigurationException confEx) {
      expBankConfig.getLogger().log(Level.WARNING,
          "Could not load experience for player [" + player.getName() + "].", confEx);
    }
  
    for (int line = 0; line < 4; line++) {
      String evaluatedLine = renderSignLines(signLines.get(line), player,
          storedPlayerExperience);
      lines[line] = JSONUtil.toJSON(evaluatedLine);
    }
  
    return lines;
  }
}
