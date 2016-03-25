package code.husky.mysql;

import code.husky.Database;
import code.husky.DatabaseConnectorException;
import code.husky.StubDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Connects to and uses a MySQL database.
 *
 * @author -_Husky_-
 * @author tips48
 */
public class MySQL extends StubDatabase implements Database {
  private final String user;
  private final String database;
  private final String password;
  private final int port;
  private final String hostname;

  /**
   * Creates a new MySQL instance.
   *
   * @param hostname
   *          Name of the host
   * @param port
   *          Port number
   * @param database
   *          Database name
   * @param username
   *          Username
   * @param password
   *          Password
   */
  public MySQL(String hostname, int port, String database, String username,
      String password) {
    this.hostname = hostname;
    this.port = port;
    this.database = database;
    this.user = username;
    this.password = password;
  }

  @Override
  public Connection openConnection() throws DatabaseConnectorException {
    try {
      if (checkConnection()) {
        return getConnection();
      }

      Class.forName("com.mysql.jdbc.Driver");
      setConnection(DriverManager.getConnection(
          "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database, this.user,
          this.password));
    } catch (SQLException | ClassNotFoundException connectEx) {
      throw new DatabaseConnectorException(connectEx);
    }

    return getConnection();
  }


}
