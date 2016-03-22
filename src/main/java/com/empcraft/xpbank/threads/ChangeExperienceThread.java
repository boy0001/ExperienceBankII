/**
 *
 */

package com.empcraft.xpbank.threads;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Thread to handle experience changes.
 */
public class ChangeExperienceThread implements Runnable {
  private Player player;
  private final int delta;
  private final ExpBankConfig config;
  private YamlLanguageProvider ylp;
  private DataHelper dh;

  /**
   * Handle experience changes for the bank.
   *
   * @param player
   *          the uuid of the player whose experience will be set.
   * @param delta
   *          the delta, added to the bank and substracted from the player.
   * @param config
   *          the config.
   * @param ylp
   *          the translation messages.
   */
  public ChangeExperienceThread(final Player player, final int delta, final ExpBankConfig config,
      YamlLanguageProvider ylp) {
    this.player = player;
    this.delta = delta;
    this.config = config;
    this.ylp = ylp;

    this.dh = new DataHelper(ylp, config);
  }

  @Override
  public void run() {
    boolean success = false;

    int actualValue = checkForMaximumDeposit();

    if (actualValue == -1) {
      // We cannot reach the database anyway.
      return;
    }

    try {
      success = dh.updatePlayerExperienceDelta(player.getUniqueId(), actualValue);
    } catch (ConfigurationException confEx) {
      config.getLogger().log(Level.WARNING,
          "Could not change experience level for [" + player.getUniqueId().toString() + "].",
          confEx);
      MessageUtils.sendMessageToConsole(ylp.getMessage("MYSQL-GET"));
      MessageUtils.sendMessageToPlayer(player, ylp.getMessage("LOST"));
    }

    if (!success) {
      config.getLogger().log(Level.WARNING,
          "Could not change experience level for [" + player.getUniqueId().toString() + "].");
      MessageUtils.sendMessageToConsole(ylp.getMessage("MYSQL-GET"));
      MessageUtils.sendMessageToPlayer(player, ylp.getMessage("LOST"));

      return;
    }

    // if everything worked, we can lower the player's xp.
    int currentExperience = player.getTotalExperience();
    player.setTotalExperience(currentExperience - actualValue);

    return;
  }

  public int checkForMaximumDeposit() {
    int maxDeposit = config.getMaxStorageForPlayer(player);
    int currentlyinstore = -1;

    try {
      currentlyinstore = dh.getSavedExperience(player);
    } catch (ConfigurationException confEx) {
      config.getLogger().log(Level.WARNING, "Could not get the player's currently stored xp.", confEx);

      return -1;
    }

    if (delta + currentlyinstore <= maxDeposit) {
      // the player can deposit everything.
      return delta;
    }

    // there is not enough limit left.
    return maxDeposit - currentlyinstore;
  }
}
