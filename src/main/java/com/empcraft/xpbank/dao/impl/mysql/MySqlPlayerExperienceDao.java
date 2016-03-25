package com.empcraft.xpbank.dao.impl.mysql;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.dao.PlayerExperienceDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class MySqlPlayerExperienceDao extends PlayerExperienceDao {

  /**
   * For mysql an unique index may improve performance.
   */
  private final String sqlCreate = "CREATE TABLE IF NOT EXISTS " + getTable()
      + " ( UUID VARCHAR(36) NOT NULL UNIQUE, EXP INT )";

  public MySqlPlayerExperienceDao(final Connection conn, final ExpBankConfig config) {
    super(conn, config);
  }

  @Override
  public boolean createTable() {
    boolean success = false;

    try {
      PreparedStatement st = getConnection().prepareStatement(sqlCreate);
      st.executeUpdate();
      success = true;
    } catch (SQLException sqlEx) {
      getLogger().log(Level.SEVERE, "Could not create player table [" + getTable() + "].", sqlEx);
    }

    return success;
  }

}
