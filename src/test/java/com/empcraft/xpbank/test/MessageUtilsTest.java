package com.empcraft.xpbank.test;

import com.empcraft.xpbank.text.MessageUtils;

import org.junit.Assert;
import org.junit.Test;

public class MessageUtilsTest {

  @Test
  public void testColorise_null() {
    String colorise = MessageUtils.colorise(null);

    Assert.assertEquals("", colorise);
  }

  @Test
  public void testColorise_bla() {
    String colorise = MessageUtils.colorise("bla");

    Assert.assertEquals("bla", colorise);
  }

  @Test
  public void testSendMessageToPlayer() {
    //fail("Not yet implemented");
  }

  @Test
  public void testSendMessageToAll() {
    //fail("Not yet implemented");
  }

}
