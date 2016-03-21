package com.empcraft.xpbank;

import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.events.SignBreakListener;
import com.empcraft.xpbank.events.SignChangeEventListener;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.logic.ExpBankPermission;
import com.empcraft.xpbank.logic.PermissionsHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.YamlLanguageProvider;
import com.empcraft.xpbank.threads.ChangeExperienceThread;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class ExpBank extends JavaPlugin implements Listener {
  private YamlConfiguration exp;
  private File expFile;
  private InSignsNano signListener;

  /**
   * In-Memory storage of players and their experience. Might reduce disk IO.
   */
  private final Map<UUID, Integer> expMap = new HashMap<>();

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
    MessageUtils.sendMessageToAll(getServer(), "&8===&a[&7EXPBANK&a]&8===");
    expFile = new File(getDataFolder() + File.separator + "xplist.yml");
    exp = YamlConfiguration.loadConfiguration(expFile);
    saveResource("english.yml", true);
    saveResource("spanish.yml", true);
    saveResource("catalan.yml", true);

    try {
      this.expConfig = new ExpBankConfig(this);
    } catch (ConfigurationException configEx) {
      getLogger().log(Level.SEVERE, "Could not read main config file.", configEx);
      MessageUtils.sendMessageToAll(getServer(), "Could not read main config file.");
    }

    try {
      ylp = new YamlLanguageProvider(expConfig.getLanguageFile(), getLogger());
    } catch (ConfigurationException configEx) {
      getLogger().log(Level.SEVERE, "Could not load Language file.", configEx);
      MessageUtils.sendMessageToAll(getServer(), "Could not get Yaml Language File.");

      // do not proceed: Can't work without ylp defined.
      return;
    }

    try {
      expMap.putAll(loadSavedExperience());
    } catch (ConfigurationException configEx) {
      getLogger().log(Level.SEVERE, "Clould not load saved data or save.", configEx);
      MessageUtils.sendMessageToAll(getServer(), ylp.getMessage("MYSQL-CONNECT"));

      // do not proceed: Don't register defunct listeners!
      return;
    }

    boolean manual = true;
    Plugin protocolPlugin = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib");

    if ((protocolPlugin != null && protocolPlugin.isEnabled())) {
      MessageUtils.sendMessageToAll(getServer(), "&aUsing ProtocolLib for packets");
      manual = false;
    }

    signListener = new InSignsNano(this, false, manual, expConfig);

    /* Register sign change event. */
    Bukkit.getServer().getPluginManager().registerEvents(new SignChangeEventListener(signListener,
        expConfig.getExperienceBankActivationString(), ylp), this);

    /* Register sign break event. */
    Bukkit.getServer().getPluginManager().registerEvents(
        new SignBreakListener(signListener, expConfig.getExperienceBankActivationString()), this);
    Bukkit.getServer().getPluginManager().registerEvents(this, this);
    BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    // Save any changes to the config
    scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
      @Override
      public void run() {
        saveConfig();
      }
    }, 24000L, 24000L);
  }

  private Map<UUID, Integer> loadSavedExperience() throws ConfigurationException {
    Map<UUID, Integer> experience = new HashMap<UUID, Integer>();

    if (getConfig().getBoolean("mysql.enabled")) {
      experience = loadExperienceFromSql();

      return experience;
    }

    // load from config.
    Set<String> players = exp.getKeys(false);

    for (String player : players) {
      int playerExp = exp.getInt(player);
      UUID uuid = UUID.fromString(player);
      experience.put(uuid, playerExp);
    }

    MessageUtils.sendMessageToAll(getServer(), ylp.getMessage("YAML"));

    return experience;
  }

  private Map<UUID, Integer> loadExperienceFromSql() throws ConfigurationException {
    MessageUtils.sendMessageToAll(getServer(), ylp.getMessage("MYSQL"));
    DataHelper dh = new DataHelper(ylp, getConfig(), getLogger());

    boolean exists = dh.createTableIfNotExists();

    if (!exists) {
      throw new ConfigurationException();
    }

    int length = dh.countPlayersInDatabase();

    if (length == 0) {
      /*
       * If there are no players in the database yet, see, if we can migrate player's exp from the
       * yaml config.
       */
      dh.converToDbIfPlayersFound(exp);
    }

    return dh.getSavedExperience();
  }

  private void runTask(final Runnable r) {
    Bukkit.getScheduler().runTaskAsynchronously(this, r);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK
        || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
      return;
    }

    Block block = event.getClickedBlock();
    if ((block.getType() == Material.SIGN_POST) || (block.getType() == Material.WALL_SIGN)) {
      Sign sign = (Sign) block.getState();
      Player player = event.getPlayer();
      String[] lines = sign.getLines();

      if (lines[0].equals(MessageUtils.colorise(getConfig().getString("text.create")))) {
        if (PermissionsHelper.playerHasPermission(player, ExpBankPermission.USE)) {
          ExperienceManager expMan = new ExperienceManager(player);
          int amount;
          int myExp = getExp(player.getUniqueId());

          if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (player.isSneaking()) {
              amount = myExp;
            } else {
              amount = expMan.getXpForLevel(expMan.getLevelForExp(expMan.getCurrentExp()) + 1)
                  - expMan.getCurrentExp();
              if (amount > myExp) {
                amount = myExp;
              }
            }

            if (player.getInventory().getItemInMainHand().getType() == Material.GLASS_BOTTLE
                && PermissionsHelper.playerHasPermission(player, ExpBankPermission.USE_BOTTLE)) {
              int bottles = player.getInventory().getItemInMainHand().getAmount();

              if (bottles * 7 > myExp) {
                MessageUtils.sendMessageToPlayer(player, ylp.getMessage("BOTTLE-ERROR"));
                return;
              } else {
                amount = bottles * 7;
                player.getInventory().getItemInMainHand().setType(Material.EXP_BOTTLE);
                event.setCancelled(true);
              }

            } else {
              expMan.changeExp(amount);
            }
          } else {
            if (player.isSneaking()) {
              amount = -expMan.getCurrentExp();
            } else {
              if (expMan.getCurrentExp() > 17) {
                amount = -(expMan.getCurrentExp()
                    - expMan.getXpForLevel(expMan.getLevelForExp(expMan.getCurrentExp()) - 1));
              } else {
                amount = -expMan.getCurrentExp();
              }
            }

            int max = getMaxExp(player);

            if (amount == 0) {
              MessageUtils.sendMessageToPlayer(player, ylp.getMessage("EXP-NONE"));
            } else if (myExp - amount > max) {
              amount = -(max - myExp);
              if (amount == 0) {
                MessageUtils.sendMessageToPlayer(player, ylp.getMessage("EXP-LIMIT"));
              }
            }
            expMan.changeExp(amount);
          }

          changeExp(player.getUniqueId(), -amount);
          signListener.scheduleUpdate(player, sign, 1);
        } else {
          MessageUtils.sendMessageToPlayer(player,
              ylp.getMessage("NOPERM").replace("{STRING}", "expbank.use" + ""));
        }
      }
    }
  }

  public int getMaxExp(Player player) {
    Set<String> nodes = getConfig().getConfigurationSection("storage").getKeys(false);
    int max = 0;

    for (String perm : nodes) {
      if ("default".equals(perm)
          || PermissionsHelper.playerHasPermission(player, "expbank.limit." + perm)) {
        int value = getConfig().getInt("storage." + perm);

        if (value > max) {
          max = value;
        }
      }
    }
    return max;
  }

  public int getExp(UUID uuid) {
    Integer value = expMap.get(uuid);

    if (value == null) {
      return 0;
    }

    return value;
  }

  public void changeExp(final UUID uuid, final int value) {
    if (exp == null) {
      Runnable changeExp = new ChangeExperienceThread(uuid, value, getConfig(), ylp, getServer(),
          getLogger());
      runTask(changeExp);
    } else {
      exp.set(uuid.toString(), value + getExp(uuid));

      try {
        exp.save(expFile);
      } catch (IOException e) {
        getLogger().log(Level.WARNING,
            "Could not save experience level for [" + uuid.toString() + "].", e);
      }
    }

    expMap.put(uuid, getExp(uuid) + value);
  }

}
