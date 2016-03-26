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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


/**
 *
 */
@PrepareForTest({ ExpBankConfig.class, JavaPlugin.class, Backend.class })
public class SqLiteDbTest {

  @Rule
  public PowerMockRule rule = new PowerMockRule();
  private ExpBankConfig config;
  private YamlLanguageProvider ylp;

  @Before
  public void setUp() throws FileNotFoundException, ConfigurationException, IOException,
      InvalidConfigurationException, DatabaseConnectorException {
    this.config = ConfigHelper.getFakeConfig().withBackend(Backend.SQLITE.toString()).build();

    if (config.getSqLiteDbFileName().exists()) {
      config.getSqLiteDbFileName().delete();
    }

    config.closeSqliteConnection();
    config.setupSqlite();

    ylp = new YamlLanguageProvider(config);
  }

  @Test
  public void testSqLite() throws ConfigurationException, FileNotFoundException, IOException,
      InvalidConfigurationException, URISyntaxException, DatabaseConnectorException {
    Assert.assertNotNull(config.getSqLiteDbFileName());
    Assert.assertNotNull(config.getSqLiteConnection());

    config.getLogger().log(Level.INFO, "Using Backend: [" + config.getBackend().name() + "].");
    config.getLogger().log(Level.INFO, "Database file: " + config.getSqLiteDbFileName());

    DataHelper dh = new DataHelper(ylp, config);
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

    config.closeSqliteConnection();

    Assert.assertTrue(config.getSqLiteDbFileName().exists());
  }

  @Test
  public void concurrencyTest() throws InterruptedException, DatabaseConnectorException {
    List<Runnable> threads = new ArrayList<>();
    final UUID testplayer = UUID.randomUUID();
    final DataHelper dh = new DataHelper(ylp, config);
    ExecutorService pool = Executors.newWorkStealingPool(10);

    boolean exists = dh.createTableIfNotExists();
    Assert.assertTrue(exists);

    dh.insertNewPlayer(testplayer);

    long starttime = System.currentTimeMillis();

    for (int ii = 0; ii < 500; ii++) {
      threads.add(new Runnable() {

        @Override
        public void run() {
          try {
            dh.updatePlayerExperienceDelta(testplayer, 1);
          } catch (DatabaseConnectorException dcEx) {
            config.getLogger().log(Level.SEVERE, "Could not update player", dcEx);
          }
        }
      });
    }

    for (Runnable runnable : threads) {
      pool.submit(runnable);
    }

    pool.shutdown();
    pool.awaitTermination(10, TimeUnit.SECONDS);

    long endtime = System.currentTimeMillis();

    long duration = endtime - starttime;
    config.getLogger().info("Insert took [" + duration + "]ms.");
    Assert.assertTrue(10000 > duration);

    int points = dh.getSavedExperience(testplayer);
    Assert.assertEquals(500, points);
  }

  @After
  public void tearDown() {
    config.getSqLiteDbFileName().delete();
  }

}
