package com.empcraft.xpbank.dao.impl.mysql;

import com.empcraft.xpbank.dao.PlayerExperienceDao;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySqlPlayerExperienceDao extends PlayerExperienceDao {

  private static final String SQL_INSERT = "INSERT INTO :table VALUES(:player, :experience)";

  public MySqlPlayerExperienceDao(final Connection conn, final FileConfiguration config,
      final Logger logger) {
    super(conn, config, logger);
  }

  @Override
  public boolean insertPlayerAndExperience(Player player, int experience) {
    boolean changed = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_INSERT);
      st.setString(0, getConfig().getString("mysql.connection.table"));
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

  @Override
  public boolean insertPlayerAndExperience(UUID playerUuid, int experience) {
    boolean changed = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(SQL_INSERT);
      st.setString(0, getConfig().getString("mysql.connection.table"));
      st.setString(1, playerUuid.toString());
      st.setInt(2, experience);
      int executeUpdate = st.executeUpdate();

      if (executeUpdate == 1) {
        changed = true;
      }
    } catch (SQLException sqlException) {
      getLogger().log(Level.SEVERE, "Could not insert player [" + playerUuid + "].",
          sqlException);
    }

    return changed;
  }

}
