/**
 *
 */

package com.empcraft.xpbank.err;

/**
 * Exception which is thrown, when this plugin is being configured.
 */
public class ConfigurationException extends Exception {
  /**
   * Serial.
   */
  private static final long serialVersionUID = 9177045275948071612L;

  public ConfigurationException() {
    super();
  }

  public ConfigurationException(Throwable sqlEx) {
    super(sqlEx);
  }

}
