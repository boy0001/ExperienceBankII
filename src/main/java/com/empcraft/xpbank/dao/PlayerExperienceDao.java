/**
 *
 */

package com.empcraft.xpbank.dao;

import code.husky.DatabaseConnectorException;

import com.empcraft.xpbank.ExpBankConfig;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Access player and experience data.
 */
public abstract class PlayerExperienceDao extends BaseDao {

  private final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + getTable()
      + " ( UUID VARCHAR(36), EXP INT )";

  private final String SQL_INSERT = "INSERT INTO " + getTable() + " VALUES(?, ?)";

  private final String SQL_COUNT = "SELECT COUNT(*) from " + getTable();

  private final String SQL_UPDATE = "UPDATE " + getTable() + " SET EXP = ? WHERE UUID = ?";

  private final String SQL_DELTA = "UPDATE ? SET EXP = EXP + ? WHERE UUID = ?";

  private final String SQL_SELECT_ALL = "SELECT UUID, EXP FROM " + getTable();

  private final String SQL_SELECT_UUID = "SELECT UUID, EXP FROM ? WHERE UUID = ?";

  public PlayerExperienceDao(final Connection conn, final ExpBankConfig config) {
    super(conn, config);
  }

  protected String getTable() {
    return getConfig().getMySqlUserTable();
  }

  public boolean createTable() {
    boolean success = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_CREATE);
      st.executeUpdate();
      success = true;
    } catch (SQLException sqlEx) {
      getLogger().log(Level.SEVERE, "Could not create player table [" + getTable() + "].", sqlEx);
    }

    return success;
  }

  public boolean insertPlayerAndExperience(Player player, int experience) {
    boolean changed = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_INSERT);
      st.setString(1, player.getUniqueId().toString());
      st.setInt(2, experience);
      int executeUpdate = st.executeUpdate();

      if (executeUpdate == 1) {
        changed = true;
      }
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not insert player [" + player.getName() + "].",
          sqlException);
    }

    return changed;
  }

  public boolean insertPlayerAndExperience(UUID playerUuid, int experience) {
    boolean changed = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_INSERT);
      st.setString(1, playerUuid.toString());
      st.setInt(2, experience);
      int executeUpdate = st.executeUpdate();

      if (executeUpdate == 1) {
        changed = true;
      }
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not insert player [" + playerUuid + "].", sqlException);
    }

    return changed;
  }

  public int countPlayers() {
    int playercount = 0;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_COUNT);
      ResultSet rs = st.executeQuery();

      if (rs.next()) {
        playercount = rs.getInt(1);
      }

      rs.close();
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not count players.", sqlException);
    }

    return playercount;
  }

  public boolean updatePlayerExperience(UUID player, int newExperience) {
    boolean success = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_UPDATE);
      st.setInt(1, newExperience);
      st.setString(2, player.toString());
      int changed = st.executeUpdate();

      if (changed > 0) {
        success = true;
      }
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE,
          "Could set experience of player [" + player.toString() + "] to [" + newExperience + "].",
          sqlException);
    }

    return success;
  }

  public boolean updatePlayerExperienceDelta(UUID player, int delta) {
    boolean success = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_DELTA);
      st.setInt(1, delta);
      st.setString(2, player.toString());
      int changed = st.executeUpdate();

      if (changed > 0) {
        success = true;
      }
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not update experience for player [" + player.toString()
          + "] with [" + delta + "] experience points.", sqlException);
    }

    return success;
  }

  public Map<UUID, Integer> getSavedExperience() throws DatabaseConnectorException {
    Map<UUID, Integer> savedExperience = new HashMap<>();

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_SELECT_ALL);
      ResultSet rs = st.executeQuery();

      while (rs.next()) {
        UUID uuid = UUID.fromString(rs.getString(1));
        Integer experience = Integer.valueOf(rs.getString(2));
        savedExperience.put(uuid, experience);
      }
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not fetch saved experience for all players.",
          sqlException);
      throw new DatabaseConnectorException(sqlException);
    }

    return savedExperience;
  }

  public int getSavedExperience(UUID uniqueId) throws DatabaseConnectorException {
    int result = 0;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_SELECT_UUID);
      ResultSet rs = st.executeQuery();

      if (rs.next()) {
        result = rs.getInt(2);
      }
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE,
          "Could not get saved experience for player with UID [" + uniqueId.toString() + "].",
          sqlException);
      throw new DatabaseConnectorException(sqlException);
    }

    return result;
  }

  public boolean insertNewPlayer(UUID uuid) {
    return insertPlayerAndExperience(uuid, 0);
  }
}
