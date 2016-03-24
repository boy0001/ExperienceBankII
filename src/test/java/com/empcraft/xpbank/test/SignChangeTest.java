package com.empcraft.xpbank.test;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.listeners.SignChangeEventListener;
import com.empcraft.xpbank.test.helpers.RunnableOfPlugin;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ExpBankConfig.class, SignChangeEvent.class, JavaPlugin.class,
    PluginDescriptionFile.class })
public class SignChangeTest {
  private static final String EXP_BANK = "[EXP]";
  private YamlLanguageProvider langYml;
  private SignChangeEventListener signChangeEventListener;
  private Player privileged;
  private Player operator;

  private SignChangeEvent validSignChangeEvent;
  private SignChangeEvent invalidSignChangeEvent;

  @Before
  public void setUp() throws ConfigurationException {
    /* Set up Bukkit */
    try {
      FakeServer fakeServer = new FakeServer();
      Bukkit.setServer(fakeServer);
    } catch (UnsupportedOperationException use) {
      //
    }

    langYml = PowerMockito.mock(YamlLanguageProvider.class);
    JavaPlugin plugin = PowerMockito.mock(JavaPlugin.class);
    PluginDescriptionFile pluginDescription = PowerMockito.mock(PluginDescriptionFile.class);
    PowerMockito.when(plugin.getDescription()).thenReturn(pluginDescription);
    PowerMockito.when(pluginDescription.getVersion()).thenReturn("test");
    ExpBankConfig expBankConfig = PowerMockito.mock(ExpBankConfig.class);
    PowerMockito.when(expBankConfig.getExperienceBankActivationString()).thenReturn(EXP_BANK);
    PowerMockito.when(expBankConfig.getPlugin()).thenReturn(plugin);

    signChangeEventListener = new SignChangeEventListener(expBankConfig, langYml);

    /* Set up sign to use */
    Block sign = PowerMockito.mock(Block.class);
    Location loc = PowerMockito.mock(Location.class);
    Sign state = PowerMockito.mock(Sign.class);
    PowerMockito.when(sign.getLocation()).thenReturn(loc);
    PowerMockito.when(sign.getState()).thenReturn(state);

    /* Set up not-[exp] change event */
    invalidSignChangeEvent = PowerMockito.mock(SignChangeEvent.class);
    Mockito.when(invalidSignChangeEvent.getLine(0)).thenReturn("bla");
    Mockito.when(invalidSignChangeEvent.getBlock()).thenReturn(sign);

    /* Set up correct change event */
    validSignChangeEvent = PowerMockito.mock(SignChangeEvent.class);
    Mockito.when(validSignChangeEvent.getLine(0)).thenReturn(EXP_BANK);
    Mockito.when(validSignChangeEvent.getBlock()).thenReturn(sign);

    /* Set up player with permission */
    privileged = Mockito.mock(Player.class);

    /* Set up op player */
    operator = Mockito.mock(Player.class);
    Mockito.when(operator.isOp()).thenReturn(true);
  }

  @Test
  public void testOnSignChange_operator_noline_valid() {
    Mockito.when(validSignChangeEvent.getPlayer()).thenReturn(operator);
    boolean expBankSign = signChangeEventListener.isExpBankSign(invalidSignChangeEvent);

    Assert.assertFalse(expBankSign);
  }

  @Test
  public void testOnSignChange_operator_noline_runnable() {
    Mockito.when(invalidSignChangeEvent.getPlayer()).thenReturn(operator);
    signChangeEventListener.onSignChange(invalidSignChangeEvent);

    Set<RunnableOfPlugin> tasks = ((FakeServer) Bukkit.getServer()).getTasks();
    Assert.assertEquals(0, tasks.size());
    tasks.clear();
  }

  @Test
  public void testOnSignChange_operator_line_valid() {
    Mockito.when(validSignChangeEvent.getPlayer()).thenReturn(operator);
    boolean expBankSign = signChangeEventListener.isExpBankSign(validSignChangeEvent);

    Assert.assertTrue(expBankSign);
  }

}
