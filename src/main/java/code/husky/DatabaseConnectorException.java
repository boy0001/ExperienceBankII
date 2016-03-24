package code.husky;

public class DatabaseConnectorException extends Exception {

  /**
   * Serial.
   */
  private static final long serialVersionUID = 4404655859575961922L;

  public DatabaseConnectorException() {
    super();
  }

  public DatabaseConnectorException(Throwable thrown) {
    super(thrown);
  }

  public DatabaseConnectorException(String reason) {
    super(reason);
  }

}
