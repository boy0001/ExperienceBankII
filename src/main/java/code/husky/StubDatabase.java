package code.husky;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class StubDatabase implements Database {
  private Connection connection = null;

  /* (non-Javadoc)
   * @see code.husky.Database#checkConnection()
   */
  @Override
  public boolean checkConnection() throws SQLException {
    return getConnection() != null && !getConnection().isClosed();
  }

  /* (non-Javadoc)
   * @see code.husky.Database#getConnection()
   */
  @Override
  public Connection getConnection() {
    return connection;
  }

  /* (non-Javadoc)
   * @see code.husky.Database#closeConnection()
   */
  @Override
  public boolean closeConnection() throws DatabaseConnectorException {
    if (getConnection() == null) {
      return true;
    }

    try {
      if (getConnection().isClosed()) {
        setConnection(null);

        return true;
      }

      getConnection().close();
    } catch (SQLException sqlEx) {
      throw new DatabaseConnectorException(sqlEx);
    }

    return true;
  }

  protected void setConnection(Connection connection) {
    this.connection = connection;
  }
}
