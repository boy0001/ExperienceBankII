package com.empcraft.xpbank.logic;

import code.husky.DatabaseConnectorException;
import code.husky.mysql.MySQL;
import code.husky.sqlite.SqLite;

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
    Connection c = null;

    switch (config.getBackend()) {
      case MYSQL: {
        c = getMySqlConnection();
        break;
      }
      case YAML: {
        c = null;
        break;
      }
      case SQLITE: {
        c = getSqLiteConnection();
        break;
      }
      default: {
        throw new DatabaseConnectorException("No such backend: + [" + config.getBackend() + "].");
      }
    }

    return c;
  }

  private Connection getMySqlConnection() throws DatabaseConnectorException {
    MySQL mySql = new MySQL(config.getMySqlHost(), config.getMySqlPort(), config.getMySqlDatabase(),
        config.getMySqlUsername(), config.getMySqlPassword());

    return mySql.openConnection();
  }

  private Connection getSqLiteConnection() throws DatabaseConnectorException {
    SqLite sqlite = new SqLite(config.getDbFileName());

    return sqlite.openConnection();
  }

  private PlayerExperienceDao getDao(Connection connection) {
    PlayerExperienceDao pd = null;

    switch (config.getBackend()) {
      case MYSQL: {
        pd = new MySqlPlayerExperienceDao(connection, config);
        break;
      }
      case SQLITE: {
        pd = new SqLitePlayerExperienceDao(connection, config);
        break;
      }
      default: {
        // empty
      }
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

    try (Connection connection = getConnection()) {
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
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not insert players into Database.", sqlEx);
    }

    return;
  }

  public int countPlayersInDatabase() throws DatabaseConnectorException {
    int playercount = 0;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      playercount = ped.countPlayers();
      config.getLogger().info("Found [" + playercount + "] players.");
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not count players in Database.", sqlEx);
    }

    return playercount;
  }

  public boolean updatePlayerExperience(UUID uuid, int newExperience)
      throws DatabaseConnectorException {
    boolean success = false;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      success = ped.updatePlayerExperience(uuid, newExperience);
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not update player experience.", sqlEx);
    }

    config.getLogger().log(Level.INFO,
        "Updated Experience of player [" + uuid.toString() + "] to [" + newExperience + "].");

    return success;
  }

  public boolean createTableIfNotExists() throws DatabaseConnectorException {
    boolean success = false;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      success = ped.createTable();
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not create Table in Database.", sqlEx);
    }

    config.getLogger().log(Level.INFO, "Created Database");

    return success;
  }

  public Map<UUID, Integer> getSavedExperience()
      throws DatabaseConnectorException {
    Map<UUID, Integer> results = new HashMap<>();

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      results.putAll(ped.getSavedExperience());
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not read existing saved exp from Database.", sqlEx);
      throw new DatabaseConnectorException(sqlEx);
    }

    config.getLogger().log(Level.INFO, "Read saved experience.");

    return results;
  }

  public int getSavedExperience(UUID uuid)
      throws DatabaseConnectorException {
    int result = 0;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      result = ped.getSavedExperience(uuid);
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not read existing saved exp from Database.",
          sqlEx);
      throw new DatabaseConnectorException(sqlEx);
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

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      success = ped.updatePlayerExperienceDelta(uuid, delta);
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not update player experience.", sqlEx);
      throw new DatabaseConnectorException(sqlEx);
    }

    config.getLogger().log(Level.INFO,
        "Updated experience of player [" + uuid.toString() + "] by [" + delta + "].");

    return success;
  }

  public boolean insertNewPlayer(UUID uuid) throws DatabaseConnectorException {
    boolean success = false;

    try (Connection connection = getConnection()) {
      PlayerExperienceDao ped = getDao(connection);
      success = ped.insertNewPlayer(uuid);
    } catch (SQLException sqlEx) {
      config.getLogger().log(Level.SEVERE, "Could not update player experience.", sqlEx);
      throw new DatabaseConnectorException(sqlEx);
    }

    config.getLogger().log(Level.INFO,
        "Inserted new player [" + uuid.toString() + "] with 0 experience.");

    return success;
  }

  public static int checkForMaximumWithdraw(Player player, int toWithdraw, final ExpBankConfig config) {
    int currentlyinstore = 0;

    currentlyinstore = config.getExperienceCache().get(player.getUniqueId()).get();

    if (currentlyinstore < toWithdraw) {
      // only get back whats inside.
      return currentlyinstore;
    }

    return toWithdraw;
  }

  public static int checkForMaximumDeposit(Player player, int toDeposit, final ExpBankConfig config) {
    int maxDeposit = config.getMaxStorageForPlayer(player);
    int currentlyinstore = 0;

    currentlyinstore = config.getExperienceCache().get(player.getUniqueId()).get();

    if (toDeposit + currentlyinstore <= maxDeposit) {
      // the player can deposit everything.
      return toDeposit;
    }

    // there is not enough limit left.
    return maxDeposit - currentlyinstore;
  }

}
