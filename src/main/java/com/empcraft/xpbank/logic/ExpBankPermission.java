package com.empcraft.xpbank.logic;

/**
 * Permissions as constants.
 */
public enum ExpBankPermission {
  /**
   * Permission to create a sign with [EXP].
   */
  EXPBANK_CREATE("expbank.create"),
  /**
   * Use. Bottles may not be included.
   */
  USE("expbank.use"),
  /**
   * Put the experience from your bank into a bottle.
   */
  USE_BOTTLE("expbank.use.bottle");

  private final String permissionNode;

  ExpBankPermission(final String permissionNode) {
    this.permissionNode = permissionNode;
  }

  /**
   * Returns the permission node associated with this permission.
   * @return the permission node as string.
   */
  public String getPermissionNode() {
    return permissionNode;
  }

}
