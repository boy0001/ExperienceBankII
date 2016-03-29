package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.events.SignSendEvent;
import com.empcraft.xpbank.logic.SignHelper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.logging.Level;

public class SignSendEventListener implements Listener {

  private ExpBankConfig config;

  public SignSendEventListener(ExpBankConfig config) {
    this.config = config;
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  public void onSignSend(SignSendEvent event) {
    String line = event.getLine(0);

    if (line == null || "".equals(line)) {
      return;
    }

    String[] signText = SignHelper.getSignText(event.getPlayer(), config);

    if (line.contains(config.getExperienceBankActivationString())) {
      for (int i = 0; i < 4; i++) {
        config.getLogger().log(Level.FINER, "Setting line " + i + " to + " + signText[i] + ".");
        event.setLine(i, signText[i]);
      }
    }
  }
}
