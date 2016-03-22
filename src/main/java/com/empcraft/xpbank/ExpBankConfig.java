package com.empcraft.xpbank;

import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.text.MessageUtils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class ExpBankConfig {
  private static final String TEXT_CREATE = "text.create";

  public final String version;

  private FileConfiguration config;
  private JavaPlugin plugin;
  private File languageFile;
  private String experienceBankActivationString;
  private boolean mySqlEnabled;

  private String mySqlHost;

  private int mySqlPort;

  private String mySqlDatabase;

  private String mySqlUsername;

  private String mySqlPassword;

  private String mySqlUserTable;

  public ExpBankConfig(final JavaPlugin plugin) throws ConfigurationException {
    this.plugin = plugin;
    this.version = plugin.getDescription().getVersion();
    this.config = plugin.getConfig();

    initEmptyconfig();
    readConfig();
  }

  public File getLanguageFile() {
    return this.languageFile;
  }

  public String getExperienceBankActivationString() {
    return this.experienceBankActivationString;
  }

  public void saveConfig() {
    this.plugin.saveConfig();
  }

  public String getMySqlHost() {
    return mySqlHost;
  }

  public int getMySqlPort() {
    return mySqlPort;
  }

  public String getMySqlDatabase() {
    return mySqlDatabase;
  }

  public String getMySqlUsername() {
    return mySqlUsername;
  }

  public String getMySqlPassword() {
    return mySqlPassword;
  }

  public boolean isMySqlEnabled() {
    return mySqlEnabled;
  }

  public String getMySqlUserTable() {
    return this.mySqlUserTable;
  }

  /**
   * Initialises this Config Object by reading elements from it.
   *
   * <p>
   * It will not read them on-the-fly. To re-read the config, you need to create a new ExpBankConfig
   * object.
   * </p>
   *
   * @throws ConfigurationException
   *           Problems reading the config.
   */
  private void readConfig() throws ConfigurationException {
    this.languageFile = new File(plugin.getDataFolder(),
        config.getString("language").toLowerCase() + ".yml");

    if (!languageFile.exists()) {
      throw new ConfigurationException();
    }

    if (!languageFile.canRead()) {
      throw new ConfigurationException();
    }

    this.experienceBankActivationString = config.getString(TEXT_CREATE);

    /* MySQL related */
    this.mySqlEnabled = config.getBoolean("mysql.enabled");
    this.mySqlHost = config.getString("mysql.connection.host");
    this.mySqlPort = config.getInt("mysql.connection.port");
    this.mySqlDatabase = config.getString("mysql.connection.database");
    this.mySqlUsername = config.getString("mysql.connection.username");
    this.mySqlPassword = config.getString("mysql.connection.password");
    this.mySqlUserTable = config.getString("mysql.connection.table");
  }

  private void initEmptyconfig() {
    Map<String, Object> options = new HashMap<>();
    config.set("version", version);
    options.put("language", "english");
    options.put("storage.default", 825);
    options.put(TEXT_CREATE, "[EXP]");
    options.put("text.1", "&8---&aEXP&8---");
    options.put("text.2", MessageUtils.MAGIC_KEYWORD_PLAYERNAME);
    options.put("text.3", MessageUtils.MAGIC_KEYWORD_STORED_XP);
    options.put("text.4", "&8---&a===&8---");
    options.put("mysql.enabled", false);
    options.put("mysql.connection.port", 3306);
    options.put("mysql.connection.host", "localhost");
    options.put("mysql.connection.username", "root");
    options.put("mysql.connection.password", "");
    options.put("mysql.connection.database", "mysql");
    options.put("mysql.connection.table", "expbank");

    for (final Entry<String, Object> node : options.entrySet()) {
      if (!config.contains(node.getKey())) {
        config.set(node.getKey(), node.getValue());
      }
    }
  }

  public String[] getSignContent() {
    String[] signContent = new String[4];

    for (int ii = 1; ii <= 4; ii++) {
      signContent[ii] = config.getString("text." + ii);
    }

    return signContent;
  }

}
