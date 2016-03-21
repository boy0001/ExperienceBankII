package com.empcraft.xpbank.test;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.InSignsNano;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.events.SignBreakListener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.block.BlockBreakEvent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BlockBreakEvent.class, ExpBankConfig.class, JavaPlugin.class,
    PluginDescriptionFile.class, FileConfiguration.class })
public class SignBreakTest {

  private static final String EXP_BANK = "[EXP]";

  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(SignBreakTest.class);

  private SignBreakListener signBreakEvent;
  private BlockBreakEvent blockBreakEvent;
  private InSignsNano signListener;
  private Sign notExpBankSign;
  private Sign validtExpBankSign;
  private Location mockLocation;

  @Before
  public void setUp() throws ConfigurationException {
    /* Set up Bukkit */
    try {
      FakeServer fakeServer = new FakeServer();
      Bukkit.setServer(fakeServer);
    } catch (UnsupportedOperationException use) {
      //
    }

    /* Plugin Description */
    PluginDescriptionFile pluginDescription = PowerMockito.mock(PluginDescriptionFile.class);
    PowerMockito.when(pluginDescription.getVersion()).thenReturn("test");

    /* Plugin internal config */
    FileConfiguration fileConfig = PowerMockito.mock(FileConfiguration.class);

    /* Plugin itself */
    JavaPlugin plugin = PowerMockito.mock(JavaPlugin.class);
    PowerMockito.when(plugin.getDescription()).thenReturn(pluginDescription);
    PowerMockito.when(plugin.getConfig()).thenReturn(fileConfig);

    /* Config */
    ExpBankConfig expBankConfig = PowerMockito.mock(ExpBankConfig.class);
    PowerMockito.when(expBankConfig.getExperienceBankActivationString()).thenReturn(EXP_BANK);

    /* Set up signListener */
    signListener = new InSignsNano(plugin, false, false, expBankConfig);

    /* The actual Break event */
    signBreakEvent = new SignBreakListener(signListener,
        expBankConfig.getExperienceBankActivationString());

    /* Mock block break event */
    blockBreakEvent = PowerMockito.mock(BlockBreakEvent.class);

    /* Mock location */
    mockLocation = Mockito.mock(Location.class);

    /* Set up fake sign */
    notExpBankSign = Mockito.mock(Sign.class);
    Mockito.when(notExpBankSign.getLine(0)).thenReturn(EXP_BANK + "1");

    /* Set up correct sign */
    validtExpBankSign = Mockito.mock(Sign.class);
    Mockito.when(validtExpBankSign.getLine(0)).thenReturn(EXP_BANK);
  }

  @Test
  public void testOnBlockBreak_Dirt() {
    Block dirtBlock = PowerMockito.mock(Block.class);
    Mockito.when(dirtBlock.getType()).thenReturn(Material.DIRT);
    Mockito.when(blockBreakEvent.getBlock()).thenReturn(dirtBlock);
    signBreakEvent.onBlockBreak(blockBreakEvent);

    int brokenSigns = signListener.getBrokenSigns().size();
    Assert.assertEquals(0, brokenSigns);

    LOG.debug("Number of destroyed signs: [{}].", brokenSigns);
  }

  @Test
  public void testOnBlockBreak_ValidWallSign() {
    Block signBlock = PowerMockito.mock(Block.class);
    Mockito.when(signBlock.getType()).thenReturn(Material.WALL_SIGN);
    Mockito.when(signBlock.getState()).thenReturn(validtExpBankSign);
    Mockito.when(signBlock.getLocation()).thenReturn(mockLocation);
    Mockito.when(blockBreakEvent.getBlock()).thenReturn(signBlock);
    signBreakEvent.onBlockBreak(blockBreakEvent);

    int brokenSigns = signListener.getBrokenSigns().size();
    Assert.assertEquals(1, brokenSigns);

    LOG.debug("Number of destroyed valid signs: [{}].", brokenSigns);
  }

  @Test
  public void testOnBlockBreak_ValidPostSign() {
    Block signBlock = PowerMockito.mock(Block.class);
    Mockito.when(signBlock.getType()).thenReturn(Material.SIGN_POST);
    Mockito.when(signBlock.getState()).thenReturn(validtExpBankSign);
    Mockito.when(signBlock.getLocation()).thenReturn(mockLocation);
    Mockito.when(blockBreakEvent.getBlock()).thenReturn(signBlock);
    signBreakEvent.onBlockBreak(blockBreakEvent);

    int brokenSigns = signListener.getBrokenSigns().size();
    Assert.assertEquals(1, brokenSigns);

    LOG.debug("Number of destroyed valid signs: [{}].", brokenSigns);
  }

  @Test
  public void testOnBlockBreak_InvalidWallSign() {
    Block signBlock = PowerMockito.mock(Block.class);
    Mockito.when(signBlock.getType()).thenReturn(Material.WALL_SIGN);
    Mockito.when(signBlock.getState()).thenReturn(notExpBankSign);
    Mockito.when(signBlock.getLocation()).thenReturn(mockLocation);
    Mockito.when(blockBreakEvent.getBlock()).thenReturn(signBlock);
    signBreakEvent.onBlockBreak(blockBreakEvent);

    int brokenSigns = signListener.getBrokenSigns().size();
    Assert.assertEquals(0, brokenSigns);

    LOG.debug("Number of destroyed valid signs: [{}].", brokenSigns);
  }
}
