package com.empcraft.xpbank.test;

import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperienceTest {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ExperienceTest.class);
  private static final String PLAYERNAME = "testPlayer1";
  private FakeServer server;

  @Before
  public void setUp() throws Exception {
    server = new FakeServer();
    server.createWorld("testWorld", Environment.NORMAL);
    Player base1 = server.createPlayer(PLAYERNAME);
    server.addPlayer(base1);
  }

  @Test
  public void test() {
    Player offlinePlayer = server.getPlayer(PLAYERNAME);
    int totalExperience = offlinePlayer.getTotalExperience();

    Assert.assertEquals(0, totalExperience);
    LOG.debug("Expierience for Player [{}]: [{}].", offlinePlayer.toString(), totalExperience);
  }

}
