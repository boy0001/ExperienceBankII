/**
 *
 */

package com.empcraft.xpbank.test;

import code.husky.Backend;
import code.husky.DatabaseConnectorException;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.test.helpers.ConfigHelper;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Level;


/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExpBankConfig.class, JavaPlugin.class, Backend.class })
public class SqLiteDbTest {

  @Test
  public void testSqLite() throws ConfigurationException, FileNotFoundException, IOException,
      InvalidConfigurationException, URISyntaxException, DatabaseConnectorException {
    ExpBankConfig config = ConfigHelper.getFakeConfig().withBackend("sqlite").build();
    YamlLanguageProvider ylp = new YamlLanguageProvider(config);

    config.getLogger().log(Level.INFO, "Using Backend: [" + config.getBackend().name() + "].");

    DataHelper dh = new DataHelper(ylp, config);
    config.getLogger().log(Level.INFO, "Database file: " + config.getDbFileName());
    Assert.assertNotNull(config.getDbFileName());
    if (config.getDbFileName().exists()) {
      config.getDbFileName().delete();
    }


    boolean exists = dh.createTableIfNotExists();
    Assert.assertTrue(exists);

    int countPlayersInDatabase = dh.countPlayersInDatabase();
    Assert.assertEquals(0, countPlayersInDatabase);

    boolean updated = dh.updatePlayerExperience(UUID.randomUUID(), 5);
    Assert.assertFalse(updated);

    boolean inserted = dh.insertNewPlayer(UUID.randomUUID());
    Assert.assertTrue(inserted);

    countPlayersInDatabase = dh.countPlayersInDatabase();
    Assert.assertEquals(1, countPlayersInDatabase);
    Assert.assertTrue(config.getDbFileName().exists());

    config.getDbFileName().delete();
  }

}
