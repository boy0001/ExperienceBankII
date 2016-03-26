package com.empcraft.xpbank;

import code.husky.DatabaseConnectorException;

import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.listeners.PlayerExperienceChangedListener;
import com.empcraft.xpbank.listeners.PlayerJoinListener;
import com.empcraft.xpbank.listeners.PlayerMoveListener;
import com.empcraft.xpbank.listeners.PlayerTeleportListener;
import com.empcraft.xpbank.listeners.SignChangeEventListener;
import com.empcraft.xpbank.listeners.SignLeftClickDepositListener;
import com.empcraft.xpbank.listeners.SignRightClickWithDrawBottleListener;
import com.empcraft.xpbank.listeners.SignRightClickWithDrawLevelListener;
import com.empcraft.xpbank.listeners.SignSneakLeftClickDepositAllListener;
import com.empcraft.xpbank.listeners.SignSneakRightClickWithDrawAllListener;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.Text;
import com.empcraft.xpbank.text.YamlLanguageProvider;
import com.empcraft.xpbank.threads.LoadExperienceOnStartupThread;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class ExpBank extends JavaPlugin {
  /**
   * Use ylp.getMessage("");
   */
  private YamlLanguageProvider ylp;
  private ExpBankConfig expConfig;

  @Override
  public void onDisable() {
    saveConfig();

    expConfig.closeSqliteConnection();
  }

  @Override
  public void onEnable() {
    MessageUtils.sendMessageToConsole("&8===&a[&7EXPBANK&a]&8===");

    try {
      saveResources();
      this.expConfig = new ExpBankConfig(this);
      this.ylp = new YamlLanguageProvider(expConfig);

      if (expConfig.getBackend() == null) {
        throw new IllegalArgumentException("Backend not specified!");
      }

      prepareDatabase();

      /* Migrate from yaml */
      migrateFromYaml();
    } catch (ConfigurationException | DatabaseConnectorException configEx) {
      getLogger().log(Level.SEVERE, "Clould not initialize plugin.", configEx);
      MessageUtils.sendMessageToConsole("Could not initialize plugin.");

      // do not proceed: Don't register defunct listeners!
      return;
    }

    LoadExperienceOnStartupThread lest = new LoadExperienceOnStartupThread(expConfig, ylp);
    Bukkit.getScheduler().runTaskAsynchronously(this, lest);

    registerEvents();
  }

  private void saveResources() {
    saveResource("english.yml", true);
    saveResource("spanish.yml", true);
    saveResource("catalan.yml", true);
    if (!new File(getDataFolder(), "config.yml").exists()) {
      saveResource("config.yml", true);
    }
  }

  private void migrateFromYaml() throws DatabaseConnectorException {
    try {
      // See if we need to migrate.
      Map<UUID, Integer> fromYaml = loadExperienceFromYaml();
      convertToDatabase(fromYaml);
      moveOldExperienceYmlFile();
    } catch (DatabaseConnectorException configEx) {
      getLogger().log(Level.SEVERE, "Clould not load saved data or save.", configEx);
      MessageUtils.sendMessageToConsole(ylp.getMessage(Text.MYSQL_CONNECT));

      throw configEx;
    }
  }

  private void registerEvents() {
    /* Register sign change event. */
    Bukkit.getServer().getPluginManager().registerEvents(
        new SignChangeEventListener(expConfig, ylp), this);

    /* Register player movement events */
    Bukkit.getServer().getPluginManager().registerEvents(new PlayerTeleportListener(expConfig),
        this);
    Bukkit.getServer().getPluginManager().registerEvents(new PlayerMoveListener(expConfig), this);
    Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoinListener(expConfig, ylp),
        this);

    /* Registere player leftclick event */
    Bukkit.getServer().getPluginManager()
        .registerEvents(new SignLeftClickDepositListener(ylp, expConfig), this);
    Bukkit.getServer().getPluginManager()
        .registerEvents(new SignSneakLeftClickDepositAllListener(ylp, expConfig), this);

    /* Register player right click events */
    Bukkit.getServer().getPluginManager()
        .registerEvents(new SignRightClickWithDrawLevelListener(ylp, expConfig), this);
    Bukkit.getServer().getPluginManager()
        .registerEvents(new SignSneakRightClickWithDrawAllListener(ylp, expConfig), this);
    Bukkit.getServer().getPluginManager()
        .registerEvents(new SignRightClickWithDrawBottleListener(ylp, expConfig), this);

    /* Register player exp change event */
    Bukkit.getServer().getPluginManager().registerEvents(new PlayerExperienceChangedListener(),
        this);
  }

  private void moveOldExperienceYmlFile() {
    File expFile = expConfig.getExperienceYmlFile();

    if (null == expFile || !expFile.exists()) {
      return;
    }

    File buFile = new File(expConfig.getExperienceYmlFile().getAbsolutePath() + ".bu");
    expFile.renameTo(buFile);
  }

  private Map<UUID, Integer> loadExperienceFromYaml() {
    Map<UUID, Integer> experience = new HashMap<>();
    File expFile = expConfig.getExperienceYmlFile();

    if (null == expFile || !expFile.exists()) {
      return experience;
    }

    if (!expFile.canRead()) {
      return experience;
    }

    YamlConfiguration exp = YamlConfiguration.loadConfiguration(expFile);
    Set<String> players = exp.getKeys(false);

    for (String player : players) {
      int playerExp = exp.getInt(player);
      UUID uuid = UUID.fromString(player);
      experience.put(uuid, playerExp);
    }

    MessageUtils.sendMessageToConsole(ylp.getMessage(Text.YAML));

    return experience;
  }

  private void prepareDatabase() throws DatabaseConnectorException {
    DataHelper dh = new DataHelper(ylp, expConfig);

    boolean exists = dh.createTableIfNotExists();

    if (!exists) {
      throw new DatabaseConnectorException();
    }

    return;
  }

  private void convertToDatabase(Map<UUID, Integer> yamlentries) throws DatabaseConnectorException {
    MessageUtils.sendMessageToConsole(ylp.getMessage(Text.MYSQL));
    DataHelper dh = new DataHelper(ylp, expConfig);

    int length = dh.countPlayersInDatabase();

    if (length != 0) {
      return;
    }

    /*
     * If there are no players in the database yet, see, if we can migrate player's exp from the
     * yaml config.
     */
    MessageUtils.sendMessageToConsole(ylp.getMessage(Text.CONVERT));
    dh.bulkSaveEntriesToDb(yamlentries);
  }

}
