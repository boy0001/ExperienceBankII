package com.empcraft.xpbank.text;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

public class YamlLanguageProvider {
  private final YamlConfiguration langYaml;
  private final Logger logger;

  public YamlLanguageProvider(final YamlConfiguration yaml, final Logger logger) {
    this.langYaml = yaml;
    this.logger = logger;
  }

  public String getMessage(String key) {
    try {
      return MessageUtils.colorise(langYaml.getString(key));
    } catch (Exception e) {
      logger.log(Level.INFO, "Could get Message for key [" + key + "].", e);

      return "";
    }
  }

}
