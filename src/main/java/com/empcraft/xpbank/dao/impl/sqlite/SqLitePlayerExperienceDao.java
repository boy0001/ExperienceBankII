package com.empcraft.xpbank.dao.impl.sqlite;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.dao.PlayerExperienceDao;

import java.sql.Connection;

public class SqLitePlayerExperienceDao extends PlayerExperienceDao {

  private final String sqlInsert = "INSERT INTO " + getTable() + " SELECT ?, ? "
      + " WHERE NOT EXISTS (SELECT 1 FROM " + getTable() + " WHERE UUID = ?);";

  public SqLitePlayerExperienceDao(final Connection conn, final ExpBankConfig config) {
    super(conn, config);
  }

  @Override
  public String getSqlInsert() {
    return sqlInsert;
  }

}
