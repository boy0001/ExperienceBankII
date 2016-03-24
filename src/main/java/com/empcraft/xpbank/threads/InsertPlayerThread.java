package com.empcraft.xpbank.threads;

import code.husky.DatabaseConnectorException;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.entity.Player;

import java.util.logging.Level;

public class InsertPlayerThread implements Runnable {

  private YamlLanguageProvider ylp;
  private ExpBankConfig config;
  private Player player;

  public InsertPlayerThread(final Player player, final ExpBankConfig config,
      YamlLanguageProvider ylp) {
    this.player = player;
    this.config = config;
    this.ylp = ylp;
  }

  @Override
  public void run() {
    DataHelper dh = new DataHelper(ylp, config);
    try {
      boolean insertNewPlayer = dh.insertNewPlayer(player.getUniqueId());

      if (!insertNewPlayer) {
        config.getLogger().log(Level.INFO, "Player already exists.");
      }
    } catch (DatabaseConnectorException dbEx) {
      config.getLogger().log(Level.WARNING,
          "Could not insert player [" + player.getUniqueId() + "] into db.",
          dbEx);
    }

  }

}
