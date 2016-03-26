/**
 *
 */
package code.husky.sqlite;

import code.husky.Database;
import code.husky.DatabaseConnectorException;
import code.husky.StubDatabase;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 */
public class SqLite extends StubDatabase implements Database {

  private File dbFile;


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
        return getConnection();
      }

      Class.forName("org.sqlite.JDBC");
      setConnection(DriverManager
          .getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath()));

      Statement journalSt = getConnection().createStatement();
      journalSt.executeUpdate("PRAGMA journal_mode = TRUNCATE");
      journalSt.close();

      Statement createStatement = getConnection().createStatement();
      createStatement.executeUpdate("PRAGMA SYNCHRONOUS = NORMAL");
      createStatement.close();
    } catch (SQLException | ClassNotFoundException sqlEx) {
      throw new DatabaseConnectorException(sqlEx);
    }

    return getConnection();
  }


}
