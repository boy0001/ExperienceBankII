package com.empcraft.xpbank.logic;

/**
 * Permissions as constants.
 */
public enum ExpBankPermission {
  /**
   * Permission to create a sign with [EXP].
   */
  EXPBANK_CREATE("expbank.create");

  private final String permissionNode;

  ExpBankPermission(final String permissionNode) {
    this.permissionNode = permissionNode;
  }

  /**
   * Returns the permission node associated with this permission.
   * @return
   */
  public String getPermissionNode() {
    return permissionNode;
  }

}
