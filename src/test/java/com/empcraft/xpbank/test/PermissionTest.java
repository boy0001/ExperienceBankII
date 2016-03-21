package com.empcraft.xpbank.test;

import static org.junit.Assert.fail;

import com.empcraft.xpbank.logic.PermissionsHelper;

import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class PermissionTest {

  private static final String USE_PERMISSION = "expbank.use";

  private Player operator;
  private Player privileged;
  private Player unprivileged;

  @Before
  public void setUp() {
    /* Set up operator */
    operator = Mockito.mock(Player.class);
    Mockito.when(operator.isOp()).thenReturn(true);

    /* Set up priviledged player */
    privileged = Mockito.mock(Player.class);
    Mockito.when(privileged.isOp()).thenReturn(false);
    Mockito.when(privileged.hasPermission(USE_PERMISSION)).thenReturn(true);

    /* Set up unprivileged player */
    unprivileged = Mockito.mock(Player.class);
    Mockito.when(unprivileged.isOp()).thenReturn(false);
    Mockito.when(unprivileged.hasPermission(USE_PERMISSION)).thenReturn(false);
  }

  @Test
  public void testPlayerHasPermission_nullConsole_anything() {
    boolean hasPermission = PermissionsHelper.playerHasPermission(null, "anything");

    Assert.assertTrue(hasPermission);
  }

  @Test
  public void testPlayerHasPermission_operator_anything() {
    boolean hasPermission = PermissionsHelper.playerHasPermission(operator, "anything");

    Assert.assertTrue(hasPermission);
  }

  @Test
  public void testPlayerHasPermission_operator_use() {
    boolean hasPermission = PermissionsHelper.playerHasPermission(operator, USE_PERMISSION);

    Assert.assertTrue(hasPermission);
  }

  @Test
  public void testPlayerHasPermission_privileged_use() {
    boolean hasPermission = PermissionsHelper.playerHasPermission(privileged, USE_PERMISSION);

    Assert.assertTrue(hasPermission);
  }

  @Test
  public void testPlayerHasPermission_unprivileged_use() {
    boolean hasPermission = PermissionsHelper.playerHasPermission(unprivileged, USE_PERMISSION);

    Assert.assertTrue(!hasPermission);
  }

}
