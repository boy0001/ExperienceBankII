package com.empcraft.xpbank.logic;

import code.husky.mysql.MySQL;

import com.empcraft.xpbank.dao.PlayerExperienceDao;
import com.empcraft.xpbank.dao.impl.mysql.MySqlPlayerExperienceDao;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataHelper {

  private YamlLanguageProvider ylp;
  private FileConfiguration config;
  private Logger logger;

  public DataHelper(final YamlLanguageProvider ylp, final FileConfiguration config,
      final Logger logger) {
    this.ylp = ylp;
    this.config = config;
    this.logger = logger;
  }

  private Connection getConnection() {
    if (config.getBoolean("mysql.enabled")) {
      return getMySqlConnection();
    }

    return getSqLiteConnection();
  }

  private Connection getMySqlConnection() {
    MySQL mySql = new MySQL(config.getString("mysql.connection.host"),
        config.getString("mysql.connection.port"), config.getString("mysql.connection.database"),
        config.getString("mysql.connection.username"),
        config.getString("mysql.connection.password"));

    return mySql.getConnection();
  }

  private Connection getSqLiteConnection() {
    // TODO: Implement SQLite
    return null;
  }

  private PlayerExperienceDao getDao(Connection connection) {
    if (config.getBoolean("mysql.enabled")) {
      return new MySqlPlayerExperienceDao(connection, config, logger);
    }

    // TODO: Implement SQLite
    return null;
  }

  /**
   * Searches the bank storage yaml file. If players were found, insert them into the database.
   *
   * @param experienceFile
   *          the experience storage file.
   */
  public void converToDbIfPlayersFound(final YamlConfiguration experienceFile) {
    Set<String> players = experienceFile.getKeys(false);

    if (null == players || players.isEmpty()) {
      // nothing to do.
      return;
    }

    try (Connection connection = getConnection()) {
      MessageUtils.sendMessageToAll(null, ylp.getMessage("CONVERT"));
      PlayerExperienceDao ped = getDao(connection);

      for (String player : players) {
        UUID uuid = UUID.fromString(player);
        int oldExperience = experienceFile.getInt(player);
        ped.insertPlayerAndExperience(uuid, Integer.valueOf(oldExperience));
      }

      MessageUtils.sendMessageToAll(null, ylp.getMessage("DONE"));
    } catch (SQLException sqlEx) {
      logger.log(Level.SEVERE, "Could not insert players into Database.", sqlEx);
    }

    return;
  }

  public int countPlayersInDatabase() {
    int playercount = 0;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      playercount = ped.countPlayers();
    } catch (SQLException sqlEx) {
      logger.log(Level.SEVERE, "Could not count players in Database.", sqlEx);
    }

    return playercount;
  }

  public boolean updatePlayerExperience(UUID uuid, int newExperience) {
    boolean success = false;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      success = ped.updatePlayerExperience(uuid, newExperience);
    } catch (SQLException sqlEx) {
      logger.log(Level.SEVERE, "Could not update player experience.", sqlEx);
    }

    return success;
  }

  public boolean createTableIfNotExists() {
    boolean success = false;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      success = ped.createTable();
    } catch (SQLException sqlEx) {
      logger.log(Level.SEVERE, "Could not create Table in Database.", sqlEx);
    }

    return success;
  }

  public Map<UUID, Integer> getSavedExperience() throws ConfigurationException {
    Map<UUID, Integer> results = new HashMap<UUID, Integer>();

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      results.putAll(ped.getSavedExperience());
    } catch (SQLException sqlEx) {
      logger.log(Level.SEVERE, "Could not read existing saved exp from Database.", sqlEx);
      throw new ConfigurationException(sqlEx);
    }

    return results;
  }

}
