package com.empcraft.xpbank;

import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.logic.PermissionsHelper;
import com.empcraft.xpbank.text.MessageUtils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

public final class ExpBankConfig {
  private static final String TEXT_CREATE = "text.create";

  private static final String[] SIGN_LINES = { "text.1", "text.2", "text.3", "text.4" };

  public final String version;

  private FileConfiguration config;
  private final JavaPlugin plugin;
  private File languageFile;
  private String experienceBankActivationString;
  private boolean mySqlEnabled;

  private String mySqlHost;

  private int mySqlPort;

  private String mySqlDatabase;

  private String mySqlUsername;

  private String mySqlPassword;

  private String mySqlUserTable;

  private File experienceYmlFile;

  private final List<String> signContent = new ArrayList<>();

  private final Logger logger;

  private final Map<String, Integer> limits = new HashMap<>();

  public ExpBankConfig(final JavaPlugin plugin) throws ConfigurationException {
    this.plugin = plugin;
    this.version = plugin.getDescription().getVersion();
    this.config = plugin.getConfig();
    this.logger = plugin.getLogger();

    initEmptyconfig();
    readConfig();
  }

  public JavaPlugin getPlugin() {
    return this.plugin;
  }

  public Logger getLogger() {
    return logger;
  }

  public File getLanguageFile() {
    return this.languageFile;
  }

  public String getExperienceBankActivationString() {
    return this.experienceBankActivationString;
  }

  public List<String> getSignContent() {
    return this.signContent;
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

  public File getExperienceYmlFile() {
    return this.experienceYmlFile;
  }

  public int getMaxStorageForPlayer(Player player) {
    int maxStorage = 0;

    for (Entry<String, Integer> permissionnode : limits.entrySet()) {
      if (!"default".equals(permissionnode.getKey()) && !PermissionsHelper
          .playerHasPermission(player, "expbank.limit." + permissionnode.getKey())) {
        continue;
      }

      int currentPermissionMax = permissionnode.getValue();

      if (currentPermissionMax > maxStorage) {
        maxStorage = currentPermissionMax;
      }
    }

    return maxStorage;
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

    /* Text Strings */
    this.experienceBankActivationString = config.getString(TEXT_CREATE);

    /* What will actually appear */
    for (String signline : SIGN_LINES) {
      this.signContent.add(config.getString(signline));
    }

    /* Old yml file backend */
    this.experienceYmlFile = new File(plugin.getDataFolder() + File.separator + "xplist.yml");

    /* MySQL related */
    this.mySqlEnabled = config.getBoolean("mysql.enabled");
    this.mySqlHost = config.getString("mysql.connection.host");
    this.mySqlPort = config.getInt("mysql.connection.port");
    this.mySqlDatabase = config.getString("mysql.connection.database");
    this.mySqlUsername = config.getString("mysql.connection.username");
    this.mySqlPassword = config.getString("mysql.connection.password");
    this.mySqlUserTable = config.getString("mysql.connection.table");

    /* Add limits */
    Set<String> storagelimits = config.getConfigurationSection("storage").getKeys(false);

    for (String limit : storagelimits) {
      this.limits.put(limit, config.getConfigurationSection("storage").getInt(limit));
    }
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

}
