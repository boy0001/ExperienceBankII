package com.empcraft.xpbank.dao.impl.mysql;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.dao.PlayerExperienceDao;
import com.empcraft.xpbank.err.ConfigurationException;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class MySqlPlayerExperienceDao extends PlayerExperienceDao {

  private static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS "
      + "? ( UUID VARCHAR(36), EXP INT )";

  private static final String SQL_INSERT = "INSERT INTO ? VALUES(?, ?)";

  private static final String SQL_COUNT = "SELECT COUNT(UUID) from ?";

  private static final String SQL_UPDATE = "UPDATE ? SET EXP = ? WHERE UUID = ?";

  private static final String SQL_DELTA = "UPDATE ? SET EXP = EXP + ? WHERE UUID = ?";

  private static final String SQL_SELECT_ALL = "SELECT UUID, EXP FROM ?";

  private static final String SQL_SELECT_UUID = "SELECT UUID, EXP FROM ? WHERE UUID = ?";

  public MySqlPlayerExperienceDao(final Connection conn, final ExpBankConfig config) {
    super(conn, config);
  }

  @Override
  public boolean createTable() {
    boolean success = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_CREATE);
      st.setString(1, getTable());
      st.executeUpdate();
      success = true;
    } catch (SQLException sqlEx) {
      getLogger().log(Level.SEVERE, "Could not create player table [" + getTable() + "].", sqlEx);
    }

    return success;
  }

  @Override
  public boolean insertPlayerAndExperience(Player player, int experience) {
    boolean changed = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_INSERT);
      st.setString(1, getTable());
      st.setString(2, player.getUniqueId().toString());
      st.setInt(3, experience);
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

  @Override
  public boolean insertPlayerAndExperience(UUID playerUuid, int experience) {
    boolean changed = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_INSERT);
      st.setString(1, getTable());
      st.setString(2, playerUuid.toString());
      st.setInt(3, experience);
      int executeUpdate = st.executeUpdate();

      if (executeUpdate == 1) {
        changed = true;
      }
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not insert player [" + playerUuid + "].", sqlException);
    }

    return changed;
  }

  @Override
  public int countPlayers() {
    int playercount = 0;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_COUNT);
      st.setString(1, getTable());
      ResultSet rs = st.executeQuery();

      if (rs.next()) {
        playercount = rs.getInt("UUID");
      }

      rs.close();
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not count players.", sqlException);
    }

    return playercount;
  }

  @Override
  public boolean updatePlayerExperience(UUID player, int newExperience) {
    boolean success = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_UPDATE);
      st.setString(1, getTable());
      st.setInt(2, newExperience);
      st.setString(3, player.toString());
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

  @Override
  public boolean updatePlayerExperienceDelta(UUID player, int delta) {
    boolean success = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_DELTA);
      st.setString(1, getTable());
      st.setInt(2, delta);
      st.setString(3, player.toString());
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

  @Override
  public Map<UUID, Integer> getSavedExperience() throws ConfigurationException {
    Map<UUID, Integer> savedExperience = new HashMap<>();

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_SELECT_ALL);
      st.setString(1, getTable());
      ResultSet rs = st.executeQuery();

      while (rs.next()) {
        UUID uuid = UUID.fromString(rs.getString(1));
        Integer experience = Integer.valueOf(rs.getString(2));
        savedExperience.put(uuid, experience);
      }
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not fetch saved experience for all players.",
          sqlException);
      throw new ConfigurationException(sqlException);
    }

    return savedExperience;
  }

  @Override
  public int getSavedExperience(UUID uniqueId) throws ConfigurationException {
    int result = 0;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_SELECT_UUID);
      st.setString(1, getTable());
      ResultSet rs = st.executeQuery();

      if (rs.next()) {
        result = rs.getInt(2);
      }
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE,
          "Could not get saved experience for player with UID [" + uniqueId.toString() + "].",
          sqlException);
      throw new ConfigurationException(sqlException);
    }

    return result;
  }

}
