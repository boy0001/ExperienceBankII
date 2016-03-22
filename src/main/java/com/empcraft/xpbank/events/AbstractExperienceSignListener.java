package com.empcraft.xpbank.events;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.event.Listener;

public abstract class AbstractExperienceSignListener implements Listener {

  private final YamlLanguageProvider ylp;
  private final ExpBankConfig config;

  public AbstractExperienceSignListener(final YamlLanguageProvider ylp,
      final ExpBankConfig config) {
    this.ylp = ylp;
    this.config = config;
  }

  protected ExpBankConfig getConfig() {
    return config;
  }

  protected YamlLanguageProvider getYlp() {
    return ylp;
  }

}
