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

  private final String sqlCreate = "CREATE TABLE IF NOT EXISTS " + getTable()
      + " ( UUID VARCHAR(36), EXP INT )";

  private final String sqlInsert = "INSERT INTO " + getTable()
      + " VALUES(?, ?) WHERE NOT EXISTS (SELECT 1 FROM " + getTable() + " WHERE UUID = ?)";

  private final String sqlCount = "SELECT COUNT(*) from " + getTable();

  private final String sqlUpdate = "UPDATE " + getTable() + " SET EXP = ? WHERE UUID = ?";

  private final String sqlDelta = "UPDATE " + getTable() + " SET EXP = EXP + ? WHERE UUID = ?";

  private final String sqlSelectAll = "SELECT UUID, EXP FROM " + getTable();

  private final String sqlSelectUuid = "SELECT UUID, EXP FROM " + getTable() + " WHERE UUID = ?";

  public PlayerExperienceDao(final Connection conn, final ExpBankConfig config) {
    super(conn, config);
  }

  protected String getTable() {
    return getConfig().getMySqlUserTable();
  }

  public boolean createTable() {
    boolean success = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(sqlCreate);
      st.executeUpdate();
      success = true;
      st.close();
    } catch (SQLException sqlEx) {
      getLogger().log(Level.SEVERE, "Could not create player table [" + getTable() + "].", sqlEx);
    }

    return success;
  }

  public boolean insertPlayerAndExperience(Player player, int experience) {
    return insertPlayerAndExperience(player.getUniqueId(), experience);
  }

  public boolean insertPlayerAndExperience(UUID playerUuid, int experience) {
    boolean changed = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(getSqlInsert());
      st.setString(1, playerUuid.toString());
      st.setInt(2, experience);
      st.setString(3, playerUuid.toString());
      int executeUpdate = st.executeUpdate();

      if (executeUpdate == 1) {
        changed = true;
      }

      st.close();
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not insert player [" + playerUuid + "].", sqlException);
    }

    return changed;
  }

  public int countPlayers() {
    int playercount = 0;

    try {
      PreparedStatement st = getConnection().prepareStatement(sqlCount);
      ResultSet rs = st.executeQuery();

      if (rs.next()) {
        playercount = rs.getInt(1);
      }

      rs.close();
      st.close();
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not count players.", sqlException);
    }

    return playercount;
  }

  public boolean updatePlayerExperience(UUID player, int newExperience) {
    boolean success = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(sqlUpdate);
      st.setInt(1, newExperience);
      st.setString(2, player.toString());
      int changed = st.executeUpdate();

      if (changed > 0) {
        success = true;
      }

      st.close();
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
      PreparedStatement st = getConnection().prepareStatement(sqlDelta);
      st.setInt(1, delta);
      st.setString(2, player.toString());
      int changed = st.executeUpdate();

      if (changed > 0) {
        success = true;
      }

      st.close();
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not update experience for player [" + player.toString()
          + "] with [" + delta + "] experience points.", sqlException);
    }

    return success;
  }

  public Map<UUID, Integer> getSavedExperience() throws DatabaseConnectorException {
    Map<UUID, Integer> savedExperience = new HashMap<>();

    try {
      PreparedStatement st = getConnection().prepareStatement(sqlSelectAll);
      ResultSet rs = st.executeQuery();

      while (rs.next()) {
        UUID uuid = UUID.fromString(rs.getString(1));
        Integer experience = Integer.valueOf(rs.getString(2));
        savedExperience.put(uuid, experience);
      }

      rs.close();
      st.close();
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
      PreparedStatement st = getConnection().prepareStatement(sqlSelectUuid);
      ResultSet rs = st.executeQuery();

      if (rs.next()) {
        result = rs.getInt(2);
      }

      rs.close();
      st.close();
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

  public String getSqlInsert() {
    return sqlInsert;
  }
}
