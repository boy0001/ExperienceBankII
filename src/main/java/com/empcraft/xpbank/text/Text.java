package com.empcraft.xpbank.text;

public enum Text {
  /**
   * String that mysql is enabled.
   */
  MYSQL,
  /**
   * MySQL connect error.
   */
  MYSQL_CONNECT, MYSQL_GET, YAML, CONVERT, SUCCESS,
  /**
   * Done/Success.
   */
  DONE, CREATE,
  /**
   * Player possibly lost XP due to a bug.
   */
  LOST,
  /**
   * Info about missing permission, replace {STRING}.
   */
  NOPERM, BOTTLE_ERROR, EXP_NONE, EXP_LIMIT;
}
