package com.empcraft.xpbank.dao;

import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.util.logging.Logger;

public abstract class BaseDao {

  private Connection connection;
  private FileConfiguration config;
  private Logger logger;

  public BaseDao(final Connection conn, final FileConfiguration config, final Logger logger) {
    this.connection = conn;
    this.config = config;
    this.logger = logger;
  }

  protected Connection getConnection() {
    return this.connection;
  }

  protected FileConfiguration getConfig() {
    return this.config;
  }

  protected Logger getLogger() {
    return this.logger;
  }
}
