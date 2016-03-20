/**
 *
 */
package com.empcraft.xpbank.threads;

import com.empcraft.xpbank.YamlLanguageProvider;
import com.empcraft.xpbank.text.MessageUtils;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ChangeExperienceThread implements Runnable {
  private Statement statement;
  private UUID uuid;
  private int value;
  private final Logger logger;
  private FileConfiguration config;
  private YamlLanguageProvider ylp;
  private Server server;

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
    try {
      statement.executeUpdate("UPDATE " + config.getString("mysql.connection.table")
          + " SET EXP=EXP+" + value + " WHERE UUID='" + uuid.toString() + "'");
      statement.close();
    } catch (SQLException e) {
      logger.log(Level.WARNING, "Could not change experience level for [" + uuid.toString() + "].",
          e);
      MessageUtils.sendMessageToAll(server, ylp.getMessage("MYSQL-GET"));
    }

    return;
  }
}
