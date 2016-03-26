package com.empcraft.xpbank.test.helpers;

import code.husky.Backend;
import code.husky.DatabaseConnectorException;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.err.ConfigurationException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigHelper {
  private static final Logger LOG = Logger.getLogger("ConfigHelper");

  private static final String testclassespath = ConfigHelper.class.getResource("/").getPath();

  private ExpBankConfig config;

  public static ConfigHelper getFakeConfig() throws ConfigurationException, FileNotFoundException,
      IOException, InvalidConfigurationException {
    ConfigHelper confighelper = new ConfigHelper();
    JavaPlugin javaPlugin = mockJavaPlugin();
    ExpBankConfig cfg = new ExpBankConfig(javaPlugin);
    confighelper.config = PowerMockito.spy(cfg);

    return confighelper;
  }

  private static JavaPlugin mockJavaPlugin()
      throws FileNotFoundException, IOException, InvalidConfigurationException {
    String topResourcePath = ConfigHelper.class.getResource("/").getPath();
    LOG.info("Using Datafolder: [" + topResourcePath + "].");
    File datafolder = new File(topResourcePath, "../classes");
    LOG.info("Using Datafolder: [" + datafolder.getAbsolutePath() + "].");
    JavaPlugin plugin = PowerMockito.mock(JavaPlugin.class);

    PluginDescriptionFile mockDescription = mockDescription();
    LOG.info("Description: [{}].".replaceFirst("\\{\\}", mockDescription.getDescription()));
    PowerMockito.when(plugin.getDescription()).thenReturn(mockDescription);

    LOG.info("Description: [{}].".replaceFirst("\\{\\}", plugin.getDescription().getDescription()));

    PowerMockito.doReturn(datafolder.getCanonicalFile()).when(plugin).getDataFolder();

    YamlConfiguration yamlConfig = getYamlConfig();
    PowerMockito.doReturn(yamlConfig).when(plugin).getConfig();

    Logger logger = Logger.getLogger("TestLogger");
    PowerMockito.doReturn(logger).when(plugin).getLogger();

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
    InputStream descFile = ConfigHelper.class.getResourceAsStream("/plugin.yml");
    InputStreamReader isr = new InputStreamReader(descFile);
    PluginDescriptionFile descriptionFile = null;

    try {
      descriptionFile = new PluginDescriptionFile(isr);
      isr.close();
      descFile.close();
    } catch (InvalidDescriptionException e) {
      LOG.log(Level.SEVERE, "Could not load plugin desc!", e);
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "Could not load plugin desc!", e);
    }

    if (descriptionFile == null) {
      throw new IllegalStateException("Description is null");
    }

    return descriptionFile;
  }

  public ConfigHelper withLanguage(String language) {
    File langFile = new File(this.config.getPlugin().getDataFolder(), language + ".yml");
    PowerMockito.when(this.config.getLanguageFile()).thenReturn(langFile);

    return this;
  }

  public ConfigHelper withBackend(String backend) throws DatabaseConnectorException {
    Backend be = Backend.getBackend(backend);
    LOG.info("Using Backend [" + be.toString() + "].");

    PowerMockito.doReturn(be).when(config).getBackend();
    config.setupSqlite();

    return this;
  }

  public ExpBankConfig build() {
    return this.config;
  }

}
