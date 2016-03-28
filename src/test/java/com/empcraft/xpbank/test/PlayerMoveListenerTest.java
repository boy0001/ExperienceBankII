package com.empcraft.xpbank.test;

import code.husky.Backend;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.listeners.PlayerMoveListener;
import com.empcraft.xpbank.test.helpers.ConfigHelper;
import com.empcraft.xpbank.test.helpers.FakeServer;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

@PrepareForTest({ ExpBankConfig.class, JavaPlugin.class, Backend.class, Chunk.class })
public class PlayerMoveListenerTest {

  @Rule
  public PowerMockRule rule = new PowerMockRule();

  private ExpBankConfig config;

  private Location location1;

  private Location location2;

  @Before
  public void setUp() throws Exception {
    FakeServer fakeServer = new FakeServer();
    World w = fakeServer.createWorld("bla", null);
    config = ConfigHelper.getFakeConfig().build();
    Location loc1 = new Location(w, 0, 0, 0);
    location1 = PowerMockito.spy(loc1);
    Chunk loc1chunk = PowerMockito.mock(Chunk.class);
    PowerMockito.doReturn(loc1chunk).when(location1).getChunk();

    Location loc2 = new Location(w, 0, 0, 0);
    location2 = PowerMockito.spy(loc2);
    Chunk loc2chunk = PowerMockito.mock(Chunk.class);
    PowerMockito.doReturn(loc2chunk).when(location2).getChunk();
  }

  @Test
  public void testOnPlayerMove() {
    PlayerMoveListener playerMoveListener = new PlayerMoveListener(config);
    Assert.assertNotNull(playerMoveListener);

    PlayerMoveEvent moveEvent = PowerMockito.mock(PlayerMoveEvent.class);
    PowerMockito.doReturn(location1).when(moveEvent).getFrom();
    PowerMockito.doReturn(location1).when(moveEvent).getTo();

    playerMoveListener.onPlayerMove(moveEvent);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testOnPlayerMoveOther() {
    PlayerMoveListener playerMoveListener = new PlayerMoveListener(config);
    Assert.assertNotNull(playerMoveListener);

    PlayerMoveEvent moveEvent = PowerMockito.mock(PlayerMoveEvent.class);
    PowerMockito.doReturn(location1).when(moveEvent).getFrom();
    PowerMockito.doReturn(location2).when(moveEvent).getTo();

    playerMoveListener.onPlayerMove(moveEvent);
  }

}
