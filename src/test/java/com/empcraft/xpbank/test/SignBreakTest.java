package com.empcraft.xpbank.test;

import com.empcraft.xpbank.InSignsNano;
import com.empcraft.xpbank.events.SignBreakListener;
import com.empcraft.xpbank.text.MessageUtils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
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
@PrepareForTest(BlockBreakEvent.class)
public class SignBreakTest {

  private static final String EXP_BANK = "{ExpBank}";

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
  public void setUp() {
    blockBreakEvent = PowerMockito.mock(BlockBreakEvent.class);
    Plugin plugin = Mockito.mock(Plugin.class);
    signListener = new InSignsNano(plugin, false, false) {
      @Override
      public String[] getValue(String[] lines, Player player, Sign sign) {
        return lines;
      }
    };
    FileConfiguration config = PowerMockito.mock(FileConfiguration.class);
    Mockito.when(config.getString("text.create")).thenReturn(EXP_BANK);
    signBreakEvent = new SignBreakListener(signListener, config);

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
