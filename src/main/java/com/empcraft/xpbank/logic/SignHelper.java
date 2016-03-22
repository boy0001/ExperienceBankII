package com.empcraft.xpbank.logic;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.threads.SingleSignUpdateThread;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

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
}
