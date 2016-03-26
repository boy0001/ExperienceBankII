package com.empcraft.xpbank;

import code.husky.Backend;

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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExpBankConfig {

  private static final String DB_FILENAME = "xp.db";

  private static final String TEXT_CREATE = "text.create";

  private static final String[] SIGN_LINES = { "text.1", "text.2", "text.3", "text.4" };

  public final String version;

  private FileConfiguration config;
  private final JavaPlugin plugin;
  private File languageFile;
  private String experienceBankActivationString;

  private final ExperienceCache experienceCache = new ExperienceCache();

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

  private Backend backend;

  public ExpBankConfig(final JavaPlugin plugin) throws ConfigurationException {
    this.plugin = plugin;
    this.version = plugin.getDescription().getVersion();
    this.config = plugin.getConfig();
    this.logger = plugin.getLogger();

    refreshConfig();
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

  public String getMySqlUserTable() {
    return this.mySqlUserTable;
  }

  public File getExperienceYmlFile() {
    return this.experienceYmlFile;
  }

  public File getDbFileName() {
    return new File(plugin.getDataFolder(), DB_FILENAME);
  }

  public Backend getBackend() {
    return this.backend;
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

  public ExperienceCache getExperienceCache() {
    return experienceCache;
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
    String languagefilepath = plugin.getDataFolder() + File.separator
        + config.getString("language").toLowerCase() + ".yml";
    this.languageFile = new File(languagefilepath);

    if (!languageFile.exists()) {
      plugin.getLogger()
          .log(Level.SEVERE,
              "Could not find language file: [" + languagefilepath + "].");
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

    /* Backend */
    this.backend = Backend.getBackend(config.getString("backend"));

    /* Old yml file backend */
    this.experienceYmlFile = new File(plugin.getDataFolder() + File.separator + "xplist.yml");

    /* MySQL related */
    this.mySqlHost = config.getString("mysql.host");
    this.mySqlPort = config.getInt("mysql.port");
    this.mySqlDatabase = config.getString("mysql.database");
    this.mySqlUsername = config.getString("mysql.username");
    this.mySqlPassword = config.getString("mysql.password");
    this.mySqlUserTable = config.getString("mysql.table");

    /* Add limits */
    Set<String> storagelimits = config.getConfigurationSection("storage").getKeys(false);

    for (String limit : storagelimits) {
      this.limits.put(limit, config.getConfigurationSection("storage").getInt(limit));
    }
  }

  private void refreshConfig() {
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
    options.put("mysql.port", 3306);
    options.put("mysql.host", "localhost");
    options.put("mysql.username", "root");
    options.put("mysql.password", "");
    options.put("mysql.database", "mysql");
    options.put("mysql.table", "expbank");
    options.put("backend", "sqlite");

    for (final Entry<String, Object> node : options.entrySet()) {
      if (!config.contains(node.getKey())) {
        config.set(node.getKey(), node.getValue());
      }
    }
  }

}
