package com.empcraft.xpbank.text;

import com.empcraft.xpbank.err.ConfigurationException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YamlLanguageProvider {
  private final YamlConfiguration langYaml;
  private final Logger logger;

  public YamlLanguageProvider(final File languageFile, final Logger logger)
      throws ConfigurationException {
    try {
      this.langYaml = YamlConfiguration.loadConfiguration(languageFile);
    } catch (IllegalArgumentException iae) {
      throw new ConfigurationException(iae);
    }

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
