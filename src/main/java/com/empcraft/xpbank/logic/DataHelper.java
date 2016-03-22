package com.empcraft.xpbank.logic;

import code.husky.mysql.MySQL;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.dao.PlayerExperienceDao;
import com.empcraft.xpbank.dao.impl.mysql.MySqlPlayerExperienceDao;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataHelper {

  private YamlLanguageProvider ylp;
  private ExpBankConfig config;

  public DataHelper(final YamlLanguageProvider ylp, final ExpBankConfig config) {
    this.ylp = ylp;
    this.config = config;
  }

  private Connection getConnection() {
    if (config.isMySqlEnabled()) {
      return getMySqlConnection();
    }

    return getSqLiteConnection();
  }

  private Connection getMySqlConnection() {
    MySQL mySql = new MySQL(config.getMySqlHost(), config.getMySqlPort(), config.getMySqlDatabase(),
        config.getMySqlUsername(), config.getMySqlPassword());

    return mySql.getConnection();
  }

  private Connection getSqLiteConnection() {
    // TODO: Implement SQLite
    return null;
  }

  private PlayerExperienceDao getDao(Connection connection) {
    if (config.isMySqlEnabled()) {
      return new MySqlPlayerExperienceDao(connection, config);
    }

    // TODO: Implement SQLite
    return null;
  }

  /**
   * Searches the bank storage yaml file. If players were found, insert them into the database.
   *
   * @param yamlentries
   *          the previously stored experience in a yaml file.
   */
  public void builkSaveEntriesToDb(Map<UUID, Integer> yamlentries) {
    if (null == yamlentries || yamlentries.isEmpty()) {
      // nothing to do.
      return;
    }

    try (Connection connection = getConnection()) {
      MessageUtils.sendMessageToConsole(ylp.getMessage("CONVERT"));
      PlayerExperienceDao ped = getDao(connection);

      for (Map.Entry<UUID, Integer> player : yamlentries.entrySet()) {
        UUID uuid = player.getKey();
        int oldExperience = player.getValue();
        ped.insertPlayerAndExperience(uuid, Integer.valueOf(oldExperience));
      }

      MessageUtils.sendMessageToConsole(ylp.getMessage("DONE"));
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not insert players into Database.", sqlEx);
    }

    return;
  }

  public int countPlayersInDatabase() {
    int playercount = 0;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      playercount = ped.countPlayers();
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not count players in Database.", sqlEx);
    }

    return playercount;
  }

  public boolean updatePlayerExperience(UUID uuid, int newExperience) {
    boolean success = false;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      success = ped.updatePlayerExperience(uuid, newExperience);
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not update player experience.", sqlEx);
    }

    return success;
  }

  public boolean createTableIfNotExists() {
    boolean success = false;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      success = ped.createTable();
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not create Table in Database.", sqlEx);
    }

    return success;
  }

  public Map<UUID, Integer> getSavedExperience() throws ConfigurationException {
    Map<UUID, Integer> results = new HashMap<UUID, Integer>();

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      results.putAll(ped.getSavedExperience());
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not read existing saved exp from Database.", sqlEx);
      throw new ConfigurationException(sqlEx);
    }

    return results;
  }

  public int getSavedExperience(UUID uuid) throws ConfigurationException {
    int result = 0;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      result = ped.getSavedExperience(uuid);
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not read existing saved exp from Database.",
          sqlEx);
      throw new ConfigurationException(sqlEx);
    }

    return result;
  }

  public int getSavedExperience(Player player) throws ConfigurationException {
    if (player == null) {
      return 0;
    }

    return getSavedExperience(player.getUniqueId());
  }

}
