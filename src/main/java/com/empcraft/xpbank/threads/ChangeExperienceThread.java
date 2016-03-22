/**
 *
 */

package com.empcraft.xpbank.threads;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.Text;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Thread to handle experience changes.
 */
public class ChangeExperienceThread implements Runnable {
  /**
   * Experience gain per bottle ranges from 3 to 11….
   */
  private static final int EXPERIENCE_PER_BOTTLE = 7;

  private Player player;
  private final int delta;
  private final ExpBankConfig config;
  private YamlLanguageProvider ylp;
  private DataHelper dh;
  private boolean deltaBottles;
  private final int numBottles;

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
    this(player, delta, config, ylp, false);
  }

  public ChangeExperienceThread(final Player player, final int delta, final ExpBankConfig config,
      YamlLanguageProvider ylp, boolean deltaBottles) {
    this.player = player;
    this.config = config;
    this.ylp = ylp;
    this.deltaBottles = deltaBottles;

    if (deltaBottles) {
      // delta = num of bottles, * experience per bottle * -1 (withdraw).
      this.delta = delta * EXPERIENCE_PER_BOTTLE * -1;
      this.numBottles = delta;
    } else {
      this.delta = delta;
      this.numBottles = 0;
    }

    this.dh = new DataHelper(ylp, config);
  }

  @Override
  public void run() {
    boolean success = false;
    int actualValue;

    if (delta < 0) {
      // player withdraws xp.
      actualValue = checkForMaximumWithdraw();
    } else {
      // player wants to store.
      actualValue = checkForMaximumDeposit();
    }

    if (numBottles > 0 && actualValue != delta) {
      // not enough experience on bank to fill all those bottles.
      MessageUtils.sendMessageToPlayer(player, ylp.getMessage(Text.BOTTLE_ERROR));

      return;
    }

    try {
      success = dh.updatePlayerExperienceDelta(player.getUniqueId(), actualValue);
    } catch (ConfigurationException confEx) {
      config.getLogger().log(Level.WARNING,
          "Could not change experience level for [" + player.getUniqueId().toString() + "].",
          confEx);
      MessageUtils.sendMessageToConsole(ylp.getMessage(Text.MYSQL_GET));
      MessageUtils.sendMessageToPlayer(player, ylp.getMessage(Text.LOST));
    }

    if (!success) {
      config.getLogger().log(Level.WARNING,
          "Could not change experience level for [" + player.getUniqueId().toString() + "].");
      MessageUtils.sendMessageToConsole(ylp.getMessage(Text.MYSQL_GET));
      MessageUtils.sendMessageToPlayer(player, ylp.getMessage(Text.LOST));

      return;
    }

    if (deltaBottles) {
      player.getInventory().getItemInMainHand().setType(Material.EXP_BOTTLE);

      return;
    }

    // if everything worked, we can lower the player's xp.
    int currentExperience = player.getTotalExperience();
    player.setTotalExperience(currentExperience - actualValue);

    return;
  }

  private int checkForMaximumWithdraw() {
    int currentlyinstore = 0;

    try {
      currentlyinstore = dh.getSavedExperience(player);
    } catch (ConfigurationException confEx) {
      config.getLogger().log(Level.WARNING, "Could not get the player's currently stored xp.",
          confEx);

      return 0;
    }

    if (currentlyinstore < (delta * -1)) {
      // only get back whats inside.
      return currentlyinstore * -1;
    }

    return delta;
  }

  private int checkForMaximumDeposit() {
    int maxDeposit = config.getMaxStorageForPlayer(player);
    int currentlyinstore = 0;

    try {
      currentlyinstore = dh.getSavedExperience(player);
    } catch (ConfigurationException confEx) {
      config.getLogger().log(Level.WARNING, "Could not get the player's currently stored xp.", confEx);

      return 0;
    }

    if (delta + currentlyinstore <= maxDeposit) {
      // the player can deposit everything.
      return delta;
    }

    // there is not enough limit left.
    return maxDeposit - currentlyinstore;
  }
}
