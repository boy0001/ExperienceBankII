package com.empcraft.xpbank.dao.impl.sqlite;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.dao.PlayerExperienceDao;

import java.sql.Connection;

public class SqLitePlayerExperienceDao extends PlayerExperienceDao {

  public SqLitePlayerExperienceDao(final Connection conn, final ExpBankConfig config) {
    super(conn, config);
  }

}
