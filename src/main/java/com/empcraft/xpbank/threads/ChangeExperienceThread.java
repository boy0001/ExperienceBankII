/**
 *
 */

package com.empcraft.xpbank.threads;

import code.husky.DatabaseConnectorException;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.Text;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Thread to handle experience changes.
 */
public class ChangeExperienceThread implements Runnable {
  private final UUID player;
  private final int delta;
  private final ExpBankConfig config;
  private YamlLanguageProvider ylp;
  private DataHelper dh;

  /**
   * Handle experience changes for the bank.
   *
   * @param playerUuid
   *          the uuid of the player whose experience will be set.
   * @param delta
   *          the delta, added to the bank and substracted from the player.
   * @param config
   *          the config.
   * @param ylp
   *          the translation messages.
   */
  public ChangeExperienceThread(final UUID playerUuid, final int delta, final ExpBankConfig config,
      YamlLanguageProvider ylp) {
    this.player = playerUuid;
    this.config = config;
    this.ylp = ylp;
    this.delta = delta;

    this.dh = new DataHelper(ylp, config);
  }

  @Override
  public void run() {
    boolean success = false;

    try {
      success = dh.updatePlayerExperienceDelta(player, delta);
    } catch (DatabaseConnectorException confEx) {
      config.getLogger().log(Level.WARNING,
          "Could not change experience level for [" + player.toString() + "].",
          confEx);
      MessageUtils.sendMessageToConsole(ylp.getMessage(Text.MYSQL_GET));
      MessageUtils.sendMessageToPlayer(player, ylp.getMessage(Text.LOST));
    }

    if (!success) {
      config.getLogger().log(Level.WARNING,
          "Could not change experience level for [" + player.toString() + "].");
      MessageUtils.sendMessageToConsole(ylp.getMessage(Text.MYSQL_GET));
      MessageUtils.sendMessageToPlayer(player, ylp.getMessage(Text.LOST));

      return;
    }

    return;
  }

}
