/**
 *
 */

package com.empcraft.xpbank.threads;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread to handle experience changes.
 */
public class ChangeExperienceThread implements Runnable {
  private UUID uuid;
  private int value;
  private final Logger logger;
  private ExpBankConfig config;
  private YamlLanguageProvider ylp;

  /**
   * Handle experience changes for the bank.
   * @param uuid the uuid of the player whose experience will be set.
   * @param value the new experience value in the bank.
   * @param config the config.
   * @param ylp the translation messages.
   * @param logger the logger.
   */
  public ChangeExperienceThread(
      final UUID uuid,
      final int value,
      final ExpBankConfig config,
      YamlLanguageProvider ylp,
      Logger logger) {
    this.uuid = uuid;
    this.value = value;
    this.logger = logger;
    this.config = config;
    this.ylp = ylp;
  }

  @Override
  public void run() {
    boolean success = false;
    try {
      DataHelper dh = new DataHelper(ylp, config, logger);
      int savedXp = dh.getSavedExperience(uuid);
      success = dh.updatePlayerExperience(uuid, savedXp + value);
    } catch (ConfigurationException confEx) {
      logger.log(Level.WARNING, "Could not change experience level for [" + uuid.toString() + "].");
      MessageUtils.sendMessageToConsole(ylp.getMessage("MYSQL-GET"));
    }

    if (!success) {
      logger.log(Level.WARNING, "Could not change experience level for [" + uuid.toString() + "].");
      MessageUtils.sendMessageToConsole(ylp.getMessage("MYSQL-GET"));
    }

    return;
  }
}
