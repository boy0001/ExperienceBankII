/**
 *
 */

package com.empcraft.xpbank.threads;

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
  private FileConfiguration config;
  private YamlLanguageProvider ylp;
  private Server server;

  /**
   * Handle experience changes for the bank.
   * @param uuid the uuid of the player whose experience will be set.
   * @param value the new experience value in the bank.
   * @param fileConfiguration the config.
   * @param ylp the translation messages.
   * @param server the server.
   * @param logger the logger.
   */
  public ChangeExperienceThread(
      final UUID uuid,
      final int value,
      final FileConfiguration fileConfiguration,
      YamlLanguageProvider ylp,
      final Server server,
      Logger logger) {
    this.uuid = uuid;
    this.value = value;
    this.logger = logger;
    this.config = fileConfiguration;
    this.ylp = ylp;
    this.server = server;
  }

  @Override
  public void run() {
    DataHelper dh = new DataHelper(ylp, config, logger);
    boolean success = dh.updatePlayerExperience(uuid, value);

    if (!success) {
      logger.log(Level.WARNING, "Could not change experience level for [" + uuid.toString() + "].");
      MessageUtils.sendMessageToAll(server, ylp.getMessage("MYSQL-GET"));
    }

    return;
  }
}
