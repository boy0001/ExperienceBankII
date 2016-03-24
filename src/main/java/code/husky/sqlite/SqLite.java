/**
 *
 */
package code.husky.sqlite;

import code.husky.Database;
import code.husky.DatabaseConnectorException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 */
public class SqLite implements Database {

  private File dbFile;

  private Connection connection = null;

  public SqLite(File dbFile) {
    this.dbFile = dbFile;
  }

  /* (non-Javadoc)
   * @see code.husky.Database#openConnection()
   */
  @Override
  public Connection openConnection() throws DatabaseConnectorException {
    try {
      if (checkConnection()) {
        return connection;
      }

      Class.forName("org.sqlite.JDBC");
      connection = DriverManager
          .getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

      Statement createStatement = connection.createStatement();
      createStatement.executeUpdate("PRAGMA SYNCHRONOUS=NORMAL");
      createStatement.close();
    } catch (SQLException | ClassNotFoundException sqlEx) {
      throw new DatabaseConnectorException(sqlEx);
    }

    return connection;
  }

  /* (non-Javadoc)
   * @see code.husky.Database#checkConnection()
   */
  @Override
  public boolean checkConnection() throws SQLException {
    return connection != null && !connection.isClosed();
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
    if (connection == null) {
      return true;
    }

    try {
      if (connection.isClosed()) {
        connection = null;

        return true;
      }

      connection.close();
    } catch (SQLException sqlEx) {
      throw new DatabaseConnectorException(sqlEx);
    }

    return true;
  }

}
