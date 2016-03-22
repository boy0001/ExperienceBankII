package com.empcraft.xpbank.dao;

import com.empcraft.xpbank.ExpBankConfig;

import java.sql.Connection;
import java.util.logging.Logger;

public abstract class BaseDao {

  private Connection connection;
  private ExpBankConfig config;

  public BaseDao(final Connection conn, final ExpBankConfig config) {
    this.connection = conn;
    this.config = config;
  }

  protected Connection getConnection() {
    return this.connection;
  }

  protected ExpBankConfig getConfig() {
    return this.config;
  }

  protected Logger getLogger() {
    return this.config.getLogger();
  }
}
