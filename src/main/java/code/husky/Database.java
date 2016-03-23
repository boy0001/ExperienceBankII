package code.husky;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract Database class, serves as a base for any connection method (MySQL, SQLite, etc.)
 *
 * @author -_Husky_-
 * @author tips48
 */
public interface Database {

  /**
   * Opens a connection with the database.
   *
   * @return Opened connection
   * @throws SQLException
   *           if the connection can not be opened
   * @throws ClassNotFoundException
   *           if the driver cannot be found
   */
  public Connection openConnection() throws DatabaseConnectorException;

  /**
   * Checks if a connection is open with the database.
   *
   * @return true if the connection is open
   * @throws SQLException
   *           if the connection cannot be checked
   */
  public boolean checkConnection() throws SQLException;

  /**
   * Gets the connection with the database.
   *
   * @return Connection with the database, null if none
   */
  public Connection getConnection();

  /**
   * Closes the connection with the database.
   *
   * @return true if successful
   * @throws SQLEDataDatabaseConnectorExceptionbaseConnectorExceptionxception
   *           if the connection cannot be closed
   */
  public boolean closeConnection() throws DatabaseConnectorException;
}
