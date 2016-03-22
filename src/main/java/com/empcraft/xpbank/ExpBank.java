package com.empcraft.xpbank;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.events.SignBreakListener;
import com.empcraft.xpbank.events.SignChangeEventListener;
import com.empcraft.xpbank.events.SignLeftClickDepositListener;
import com.empcraft.xpbank.events.SignRightClickWithdrawLevelListener;
import com.empcraft.xpbank.events.SignSneakLeftClickDepositAllListener;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.logic.ExpBankPermission;
import com.empcraft.xpbank.logic.PermissionsHelper;
import com.empcraft.xpbank.logic.SignHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.YamlLanguageProvider;
import com.empcraft.xpbank.threads.ChangeExperienceThread;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class ExpBank extends JavaPlugin implements Listener {
  private InSignsNano signListener;

  /**
   * Use ylp.getMessage("");
   */
  private YamlLanguageProvider ylp;
  private ExpBankConfig expConfig;

  @Override
  public void onDisable() {
    saveConfig();
  }

  @Override
  public void onEnable() {
    MessageUtils.sendMessageToConsole("&8===&a[&7EXPBANK&a]&8===");

    try {
      saveResources();
      this.expConfig = new ExpBankConfig(this);
      this.ylp = new YamlLanguageProvider(expConfig);

      prepareDatabase();

      /* Migrate from yaml */
      migrateFromYaml();
    } catch (ConfigurationException configEx) {
      getLogger().log(Level.SEVERE, "Clould not initialize plugin.", configEx);
      MessageUtils.sendMessageToConsole("Could not initialize plugin.");

      // do not proceed: Don't register defunct listeners!
      return;
    }

    boolean manual = true;
    Plugin protocolPlugin = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib");

    if (protocolPlugin != null && protocolPlugin.isEnabled()) {
      MessageUtils.sendMessageToConsole("&aUsing ProtocolLib for packets");
      manual = false;
      ProtocolManager protocolmanager = ProtocolLibrary.getProtocolManager();
      protocolmanager.addPacketListener(new SignInterceptor(this, ylp, expConfig));
    }

    signListener = new InSignsNano(false, manual, expConfig);

    registerEvents();
  }

  private void saveResources() {
    saveResource("english.yml", true);
    saveResource("spanish.yml", true);
    saveResource("catalan.yml", true);
  }

  private void migrateFromYaml() throws ConfigurationException {
    try {
      // See if we need to migrate.
      Map<UUID, Integer> fromYaml = loadExperienceFromYaml();
      convertToDatabase(fromYaml);
      moveOldExperienceYmlFile();
    } catch (ConfigurationException configEx) {
      getLogger().log(Level.SEVERE, "Clould not load saved data or save.", configEx);
      MessageUtils.sendMessageToConsole(ylp.getMessage("MYSQL-CONNECT"));

      throw configEx;
    }
  }

  private void registerEvents() {
    /* Register sign change event. */
    Bukkit.getServer().getPluginManager().registerEvents(
        new SignChangeEventListener(expConfig, ylp), this);

    /* Register sign break event. */
    Bukkit.getServer().getPluginManager().registerEvents(
        new SignBreakListener(signListener, expConfig.getExperienceBankActivationString()), this);

    /* Registere player leftclick event */
    Bukkit.getServer().getPluginManager()
        .registerEvents(new SignLeftClickDepositListener(ylp, expConfig), this);
    Bukkit.getServer().getPluginManager()
        .registerEvents(new SignSneakLeftClickDepositAllListener(ylp, expConfig), this);

    /* Register playre right click events */
    Bukkit.getServer().getPluginManager()
        .registerEvents(new SignRightClickWithdrawLevelListener(ylp, expConfig), this);

    /*
     * All other events. TODO: Remove.
     */
    Bukkit.getServer().getPluginManager().registerEvents(this, this);
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

    MessageUtils.sendMessageToConsole(ylp.getMessage("YAML"));

    return experience;
  }

  private void prepareDatabase() throws ConfigurationException {
    DataHelper dh = new DataHelper(ylp, expConfig);

    boolean exists = dh.createTableIfNotExists();

    if (!exists) {
      throw new ConfigurationException();
    }

    return;
  }

  private void convertToDatabase(Map<UUID, Integer> yamlentries) throws ConfigurationException {
    MessageUtils.sendMessageToConsole(ylp.getMessage("MYSQL"));
    DataHelper dh = new DataHelper(ylp, expConfig);

    int length = dh.countPlayersInDatabase();

    if (length != 0) {
      return;
    }

    /*
     * If there are no players in the database yet, see, if we can migrate player's exp from the
     * yaml config.
     */
    dh.builkSaveEntriesToDb(yamlentries);
  }

  private void runTask(final Runnable r) {
    Bukkit.getScheduler().runTaskAsynchronously(this, r);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
      return;
    }

    Player player = event.getPlayer();

    if (!SignHelper.isExperienceBankSignBlock(event.getClickedBlock(), expConfig)) {
      return;
    }

    if (!PermissionsHelper.playerHasPermission(player, ExpBankPermission.USE)) {
      MessageUtils.sendMessageToPlayer(player,
          ylp.getMessage("NOPERM").replace("{STRING}", "expbank.use" + ""));
      return;
    }

    ExperienceManager expMan = new ExperienceManager(player);
    int amount = 0;
    int myExp = 0; // TODO: getExp(player.getUniqueId());

    if (player.isSneaking()) {
      amount = myExp;
    }

    if (player.getInventory().getItemInMainHand().getType() == Material.GLASS_BOTTLE
        && PermissionsHelper.playerHasPermission(player, ExpBankPermission.USE_BOTTLE)) {
      int bottles = player.getInventory().getItemInMainHand().getAmount();

      if (bottles * 7 > myExp) {
        MessageUtils.sendMessageToPlayer(player, ylp.getMessage("BOTTLE-ERROR"));
        return;
      } else {
        player.getInventory().getItemInMainHand().setType(Material.EXP_BOTTLE);
        event.setCancelled(true);
      }

    } else {
      expMan.changeExp(amount);
    }

    Sign sign = (Sign) event.getClickedBlock().getState();
    SignHelper.updateSign(player, sign, expConfig);
  }

  public void changeExpOnBank(final Player player, final int delta) {
    Runnable changeExp = new ChangeExperienceThread(player, delta, expConfig, ylp);
    runTask(changeExp);
  }

}
