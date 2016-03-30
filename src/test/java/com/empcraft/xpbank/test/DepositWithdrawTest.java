package com.empcraft.xpbank.test;

import com.empcraft.xpbank.logic.DataHelper;

import org.junit.Assert;
import org.junit.Test;

public class DepositWithdrawTest {

  @Test
  public void testCheckForMaximumDeposit_bankFull() {
    int inBank = 825;
    int depositAmount = 100;
    int inHand = 100;
    int maxInBankAllowed = 825;

    int checkForMaximumDeposit = DataHelper.checkForMaximumDeposit(inBank, depositAmount, inHand,
        maxInBankAllowed);
    Assert.assertEquals(0, checkForMaximumDeposit);
  }

  @Test
  public void testCheckForMaximumDeposit_handEmpty() {
    int inBank = 10;
    int depositAmount = 100;
    int inHand = 0;
    int maxInBankAllowed = 825;

    int checkForMaximumDeposit = DataHelper.checkForMaximumDeposit(inBank, depositAmount, inHand,
        maxInBankAllowed);
    Assert.assertEquals(0, checkForMaximumDeposit);
  }

  @Test
  public void testCheckForMaximumDeposit_handAlmostEmpty() {
    int inBank = 10;
    int depositAmount = 100;
    int inHand = 50;
    int maxInBankAllowed = 825;

    int checkForMaximumDeposit = DataHelper.checkForMaximumDeposit(inBank, depositAmount, inHand,
        maxInBankAllowed);
    Assert.assertEquals(50, checkForMaximumDeposit);
  }

  @Test
  public void testCheckForMaximumWithdraw_bankEmpty() {
    int inBank = 0;
    int withdrawAmount = 100;

    int actualWithdrawAmount = DataHelper.checkForMaxumumWithdraw(inBank, withdrawAmount);
    Assert.assertEquals(0, actualWithdrawAmount);
  }

  @Test
  public void testCheckForMaximumWithdraw_bankAlmostEmpty() {
    int inBank = 10;
    int withdrawAmount = 100;

    int actualWithdrawAmount = DataHelper.checkForMaxumumWithdraw(inBank, withdrawAmount);
    Assert.assertEquals(10, actualWithdrawAmount);
  }

  @Test
  public void testCheckForMaximumWithdraw_bankFull() {
    int inBank = 810;
    int withdrawAmount = 100;

    int actualWithdrawAmount = DataHelper.checkForMaxumumWithdraw(inBank, withdrawAmount);
    Assert.assertEquals(100, actualWithdrawAmount);
  }

}
