/**
 *
 */

package com.empcraft.xpbank.logic;

import org.bukkit.entity.Player;

/**
 * Methods to help checking or defining permissions.
 */
public final class PermissionsHelper {

  /**
   * Private hidden utility constructor.
   */
  private PermissionsHelper() {}

  /**
   * Checks, if player player has the permission perm.
   * @param player The player to check.
   * @param perm the permission to check.
   * @return true, if player has the specified permission (directly or indirectly).
   */
  public static boolean playerHasPermission(Player player, String perm) {
    String[] nodes = perm.split("\\.");
    String node = "";

    if (player == null || player.isOp()) {
      // console users and operators.
      return true;
    } else if (player.hasPermission(perm)) {
      return true;
    } else {
      for (int i = 0; i < nodes.length - 1; i++) {
        node += nodes[i] + ".";

        if (player.hasPermission(node + "*")) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Checks, if player player has the permission perm.
   * @param player The player to check.
   * @param permisson the permission to check.
   * @return true, if player has the specified permission (directly or indirectly).
   */
  public static boolean playerHasPermission(Player player, ExpBankPermission permission) {
    return playerHasPermission(player, permission.getPermissionNode());
  }

}
