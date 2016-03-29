package com.empcraft.xpbank.logic;

import code.husky.Backend;
import code.husky.DatabaseConnectorException;
import code.husky.mysql.MySQL;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.dao.PlayerExperienceDao;
import com.empcraft.xpbank.dao.impl.mysql.MySqlPlayerExperienceDao;
import com.empcraft.xpbank.dao.impl.sqlite.SqLitePlayerExperienceDao;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.Text;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataHelper {

  private YamlLanguageProvider ylp;
  private ExpBankConfig config;

  public DataHelper(final YamlLanguageProvider ylp, final ExpBankConfig config) {
    this.ylp = ylp;
    this.config = config;
  }

  private Connection getConnection() throws DatabaseConnectorException {
    Connection connection;

    switch (config.getBackend()) {
      case MYSQL:
        connection = getMySqlConnection();
        break;
      case YAML:
        connection = null;
        break;
      case SQLITE:
        connection = config.getSqLiteConnection();
        break;
      default:
        throw new DatabaseConnectorException("No such backend: + [" + config.getBackend() + "].");
    }

    return connection;
  }

  private Connection getMySqlConnection() throws DatabaseConnectorException {
    MySQL mySql = new MySQL(config.getMySqlHost(), config.getMySqlPort(), config.getMySqlDatabase(),
        config.getMySqlUsername(), config.getMySqlPassword());

    return mySql.openConnection();
  }

  private PlayerExperienceDao getDao(Connection connection) {
    PlayerExperienceDao pd = null;

    switch (config.getBackend()) {
      case MYSQL:
        pd = new MySqlPlayerExperienceDao(connection, config);
        break;
      case SQLITE:
        pd = new SqLitePlayerExperienceDao(connection, config);
        break;
      default:
        break;
    }

    return pd;
  }

  /**
   * Searches the bank storage yaml file. If players were found, insert them into the database.
   *
   * @param yamlentries
   *          the previously stored experience in a yaml file.
   * @throws DatabaseConnectorException
   */
  public void bulkSaveEntriesToDb(Map<UUID, Integer> yamlentries) throws DatabaseConnectorException {
    if (null == yamlentries || yamlentries.isEmpty()) {
      // nothing to do.
      return;
    }

    Connection connection = null;

    try {
      connection = getConnection();
      MessageUtils.sendMessageToConsole(ylp.getMessage(Text.CONVERT));
      PlayerExperienceDao ped = getDao(connection);

      for (Map.Entry<UUID, Integer> player : yamlentries.entrySet()) {
        UUID uuid = player.getKey();
        int oldExperience = player.getValue();
        ped.insertPlayerAndExperience(uuid, oldExperience);
        config.getLogger().log(Level.INFO,
            "Inserted player [" + player.getKey().toString() + "] into the DB.");
      }

      MessageUtils.sendMessageToConsole(ylp.getMessage(Text.DONE));
    } finally {
      specialCloseConnection(connection);
    }

    return;
  }

  /**
   * Null- and exception safe method to close connections.
   *
   * <p>
   * <b>SQLite</b>-Conneciton may not be closed.
   * </p>
   *
   * @param connection
   *          the conneciton to be closed.
   */
  private void specialCloseConnection(Connection connection) {
    if (Backend.SQLITE.equals(config.getBackend())) {
      // Don't close SQLite connections.
      return;
    }

    if (connection == null) {
      return;
    }

    try {
      if (connection.isClosed()) {
        return;
      }

      connection.close();
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.WARNING, "Could not close connection.", sqlEx);
    }

    return;
  }

  public int countPlayersInDatabase() throws DatabaseConnectorException {
    int playercount = 0;

    Connection connection = null;
    try {
      connection = getConnection();
      PlayerExperienceDao ped = getDao(connection);
      playercount = ped.countPlayers();
      config.getLogger().info("Found [" + playercount + "] players.");
    } finally {
      specialCloseConnection(connection);
    }

    return playercount;
  }

  public boolean updatePlayerExperience(UUID uuid, int newExperience)
      throws DatabaseConnectorException {
    boolean success = false;

    Connection connection = null;

    try {
      connection = getConnection();
      PlayerExperienceDao ped = getDao(connection);
      success = ped.updatePlayerExperience(uuid, newExperience);
    } finally {
      specialCloseConnection(connection);
    }

    config.getLogger().log(Level.INFO,
        "Updated Experience of player [" + uuid.toString() + "] to [" + newExperience + "].");

    return success;
  }

  public boolean createTableIfNotExists() throws DatabaseConnectorException {
    boolean success = false;

    Connection connection = null;

    try {
      connection = getConnection();
      PlayerExperienceDao ped = getDao(connection);
      success = ped.createTable();
    } finally {
      specialCloseConnection(connection);
    }

    config.getLogger().log(Level.INFO, "Created Database");

    return success;
  }

  public Map<UUID, Integer> getSavedExperience()
      throws DatabaseConnectorException {
    Map<UUID, Integer> results = new HashMap<>();
    Connection connection = null;

    try {
      connection = getConnection();
      PlayerExperienceDao ped = getDao(connection);
      results.putAll(ped.getSavedExperience());
    } finally {
      specialCloseConnection(connection);
    }

    config.getLogger().log(Level.INFO, "Read saved experience.");

    return results;
  }

  public int getSavedExperience(UUID uuid)
      throws DatabaseConnectorException {
    int result = 0;
    Connection connection = null;

    try {
      connection = getConnection();
      PlayerExperienceDao ped = getDao(connection);
      result = ped.getSavedExperience(uuid);
    } finally {
      specialCloseConnection(connection);
    }

    config.getLogger().log(Level.INFO,
        "Experience for player [" + uuid.toString() + "] is [" + result + "]xp in bank.");

    return result;
  }

  public int getSavedExperience(Player player)
      throws DatabaseConnectorException {
    if (player == null) {
      return 0;
    }

    int result = getSavedExperience(player.getUniqueId());

    config.getLogger().log(Level.INFO,
        "Experience for player [" + player.getName() + "] is [" + result + "]xp in bank.");

    return result;
  }

  public boolean updatePlayerExperienceDelta(UUID uuid, int delta)
      throws DatabaseConnectorException {
    boolean success = false;
    Connection connection = null;

    try {
      connection = getConnection();
      PlayerExperienceDao ped = getDao(connection);
      success = ped.updatePlayerExperienceDelta(uuid, delta);
    } finally {
      specialCloseConnection(connection);
    }

    config.getLogger().log(Level.INFO,
        "Updated experience of player [" + uuid.toString() + "] by [" + delta + "].");

    return success;
  }

  public boolean insertNewPlayer(UUID uuid) throws DatabaseConnectorException {
    boolean success = false;
    Connection connection = null;

    try {
      connection = getConnection();
      PlayerExperienceDao ped = getDao(connection);
      success = ped.insertNewPlayer(uuid);
    } finally {
      specialCloseConnection(connection);
    }

    config.getLogger().log(Level.INFO,
        "Inserted new player [" + uuid.toString() + "] with 0 experience.");

    return success;
  }

  public static int checkForMaximumWithdraw(Player player, int toWithdraw, final ExpBankConfig config) {
    int currentlyinstore;

    currentlyinstore = config.getExperienceCache().get(player.getUniqueId()).get();

    if (currentlyinstore < toWithdraw) {
      // only get back whats inside.
      config.getLogger().log(
          Level.INFO, "Player can only with draw amount because he has no more inside: "
              + Integer.toString(currentlyinstore));
      return currentlyinstore;
    }

    return toWithdraw;
  }

  public static int checkForMaximumDeposit(Player player, int toDeposit, final ExpBankConfig config) {
    int maxDeposit = config.getMaxStorageForPlayer(player);
    config.getLogger().log(Level.INFO, "Player can deposit: " + Integer.toString(maxDeposit));
    int currentlyinstore;

    currentlyinstore = config.getExperienceCache().get(player.getUniqueId()).get();

    if (toDeposit + currentlyinstore <= maxDeposit) {
      // the player can deposit everything.
      config.getLogger().log(Level.INFO, "Player can deposit all: " + Integer.toString(toDeposit));
      return toDeposit;
    }

    // there is not enough limit left.
    config.getLogger().log(Level.INFO, "Player can deposit only a part because there is a limit: "
        + Integer.toString(maxDeposit - currentlyinstore));
    return maxDeposit - currentlyinstore;
  }

}
