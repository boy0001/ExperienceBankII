package com.empcraft.xpbank.test;

import com.empcraft.xpbank.logic.ExperienceLevelCalculator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExperienceCalculatorTest {
  /**
   * Logger.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ExperienceCalculatorTest.class);

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testGetLevel0() {
    int level = ExperienceLevelCalculator.getLevel(0);

    Assert.assertTrue(level == 0);
  }

  @Test
  public void testGetLevel6() {
    int level = ExperienceLevelCalculator.getLevel(6);

    Assert.assertTrue(level == 0);
  }

  @Test
  public void testGetLevel7() {
    int level = ExperienceLevelCalculator.getLevel(7);

    Assert.assertTrue(level == 1);
  }

  @Test
  public void testGetLevel_Negative() {
    int level = ExperienceLevelCalculator.getLevel(-10);

    Assert.assertEquals(0, level);
  }

  @Test
  public void testGetMinimumRequiredXpForLevel_0() {
    int minXp = ExperienceLevelCalculator.getMinExperienceForLevel(0);

    Assert.assertEquals(0, minXp);
  }

  @Test
  public void testGetMinimumRequiredXpForLevel_MaxLevel() {
    int minXp = ExperienceLevelCalculator.getMinExperienceForLevel(
        ExperienceLevelCalculator.getMaxLevel());

    Assert.assertEquals(2920, minXp);
  }

  @Test
  public void testGetMinimumRequiredXpForLevel_MaxInt() {
    int minXp = ExperienceLevelCalculator.getMinExperienceForLevel(
        Integer.MAX_VALUE);

    Assert.assertEquals(2920, minXp);
  }

}
