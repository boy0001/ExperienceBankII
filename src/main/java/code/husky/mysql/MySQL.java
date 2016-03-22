package code.husky.mysql;

import code.husky.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Connects to and uses a MySQL database.
 *
 * @author -_Husky_-
 * @author tips48
 */
public class MySQL implements Database {
  private final String user;
  private final String database;
  private final String password;
  private final int port;
  private final String hostname;

  private Connection connection;

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
    this.connection = null;
  }

  @Override
  public Connection openConnection() throws SQLException, ClassNotFoundException {
    if (checkConnection()) {
      return connection;
    }
    Class.forName("com.mysql.jdbc.Driver");
    connection = DriverManager.getConnection(
        "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database, this.user,
        this.password);
    return connection;
  }

  @Override
  public boolean checkConnection() throws SQLException {
    return connection != null && !connection.isClosed();
  }

  @Override
  public Connection getConnection() {
    return connection;
  }

  @Override
  public boolean closeConnection() throws SQLException {
    if (connection == null) {
      return false;
    }
    connection.close();
    return true;
  }

  @Override
  public ResultSet querySQL(String query) throws SQLException, ClassNotFoundException {
    if (checkConnection()) {
      openConnection();
    }

    Statement statement = connection.createStatement();

    ResultSet result = statement.executeQuery(query);

    statement.close();

    return result;
  }

  @Override
  public int updateSQL(String query) throws SQLException, ClassNotFoundException {
    if (checkConnection()) {
      openConnection();
    }

    Statement statement = connection.createStatement();

    int result = statement.executeUpdate(query);

    statement.close();

    return result;
  }

}
