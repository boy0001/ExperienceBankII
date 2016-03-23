package com.empcraft.xpbank.test.helpers;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.err.ConfigurationException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

public class ConfigHelper {
  private static final Logger LOG = Logger.getLogger("ConfigHelper");

  private static final String testclassespath = ConfigHelper.class.getResource("/").getPath();

  private ExpBankConfig config;

  public static ConfigHelper getFakeConfig() throws ConfigurationException, FileNotFoundException,
      IOException, InvalidConfigurationException {
    ConfigHelper confighelper = new ConfigHelper();
    confighelper.config = new ExpBankConfig(mockJavaPlugin());

    return confighelper;
  }

  private static JavaPlugin mockJavaPlugin()
      throws FileNotFoundException, IOException, InvalidConfigurationException {
    String topResourcePath = ConfigHelper.class.getResource("/").getPath();
    LOG.info("Using Datafolder: [" + topResourcePath + "].");
    File datafolder = new File(topResourcePath, "../classes");
    LOG.info("Using Datafolder: [" + datafolder.getAbsolutePath() + "].");
    JavaPlugin plugin = PowerMockito.mock(JavaPlugin.class);

    PowerMockito.when(plugin.getDataFolder()).thenReturn(datafolder);
    PowerMockito.when(plugin.getLogger()).thenReturn(Logger.getLogger("TestLogger"));
    PowerMockito.when(plugin.getDescription()).thenReturn(mockDescription());
    PowerMockito.when(plugin.getConfig()).thenReturn(getYamlConfig());

    return plugin;
  }

  private static YamlConfiguration getYamlConfig()
      throws FileNotFoundException, IOException, InvalidConfigurationException {
    File configfile = new File(testclassespath, "config.yml");
    LOG.info("Using config file [" + configfile.getAbsolutePath() + "].");
    YamlConfiguration configuration = new YamlConfiguration();
    configuration.load(configfile.getAbsolutePath());

    return configuration;
  }

  private static PluginDescriptionFile mockDescription() {
    PluginDescriptionFile descriptionFile = new PluginDescriptionFile("ExpBank",
        "PluginTest: Version", "com.empcraft.xpbank.ExpBank");

    return descriptionFile;
  }

  public ConfigHelper withLanguage(String language) {
    File langFile = new File(this.config.getPlugin().getDataFolder(), language + ".yml");
    PowerMockito.when(this.config.getLanguageFile()).thenReturn(langFile);

    return this;
  }

  public ExpBankConfig build() {
    return this.config;
  }

}
