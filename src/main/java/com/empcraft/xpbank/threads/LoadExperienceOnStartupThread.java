package com.empcraft.xpbank.threads;

import code.husky.DatabaseConnectorException;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.Bukkit;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class LoadExperienceOnStartupThread implements Runnable {

  private final YamlLanguageProvider ylp;
  private final ExpBankConfig config;

  public LoadExperienceOnStartupThread(final ExpBankConfig config, final YamlLanguageProvider ylp) {
    this.ylp = ylp;
    this.config = config;
  }

  @Override
  public void run() {
    DataHelper dh = new DataHelper(ylp, config);
    Map<UUID, Integer> savedExperience;

    try {
      savedExperience = dh.getSavedExperience();

      for (Entry<UUID, Integer> entry : savedExperience.entrySet()) {
        config.getLogger().log(Level.INFO, "loaded [" + entry.getValue() + "]xp for player ["
            + Bukkit.getServer().getPlayer(entry.getKey()) + "].");
        config.getExperienceCache().put(entry.getKey(), new AtomicInteger(entry.getValue()));
      }
    } catch (DatabaseConnectorException dbEx) {
      config.getLogger().log(Level.SEVERE, "Could not load old experience!", dbEx);
    }


  }

}
