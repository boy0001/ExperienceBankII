package com.empcraft.xpbank.test;

import code.husky.Backend;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.test.helpers.ConfigHelper;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExpBankConfig.class, SignChangeEvent.class, JavaPlugin.class,
    PluginDescriptionFile.class })
public class ExpBankConfigTest {

  private ExpBankConfig config;
  private YamlLanguageProvider ylp;

  @Before
  public void setUp() throws ConfigurationException, FileNotFoundException, IOException,
      InvalidConfigurationException {
    this.config = ConfigHelper.getFakeConfig().withLanguage("english")
        .withBackend(Backend.YAML.toString()).build();
    this.ylp = new YamlLanguageProvider(config);
  }

  @Test
  public void testBackend() {
    Backend backend = config.getBackend();
    Assert.assertEquals(Backend.YAML, backend);
  }

  @Test
  public void testLanguage() {
    File languageFile = config.getLanguageFile();
    Assert.assertNotNull(languageFile);
  }

  @Test
  public void testDefaultMysqlParameters() {
    String mySqlDatabase = config.getMySqlDatabase();
    String mySqlHost = config.getMySqlHost();
    String mySqlPassword = config.getMySqlPassword();
    int mySqlPort = config.getMySqlPort();
    String mySqlUsername = config.getMySqlUsername();
    String mySqlUserTable = config.getMySqlUserTable();

    Assert.assertNotNull(mySqlDatabase);
    Assert.assertNotNull(mySqlHost);
    Assert.assertNotNull(mySqlPassword);
    Assert.assertFalse(mySqlPort == 0);
    Assert.assertNotNull(mySqlUsername);
    Assert.assertNotNull(mySqlUserTable);
  }
}
