package com.empcraft.xpbank.test;

import code.husky.DatabaseConnectorException;

import com.empcraft.xpbank.err.ConfigurationException;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

public class ExceptionTests {

  private static final String COULD_NOT_CONNECT_TO_DATABASE = "Could not connect to database.";

  @Test(expected = DatabaseConnectorException.class)
  public void testThrow_DatabaseConnectionException() throws DatabaseConnectorException {
    SQLException sqlException = new SQLException(COULD_NOT_CONNECT_TO_DATABASE);
    throw new DatabaseConnectorException(sqlException);
  }

  @Test
  public void testContent_DatabaseConnectionException() throws DatabaseConnectorException {
    SQLException sqlException = new SQLException(COULD_NOT_CONNECT_TO_DATABASE);
    DatabaseConnectorException databaseConnectorException = new DatabaseConnectorException(
        sqlException);
    Assert.assertNotNull(databaseConnectorException);
    Assert.assertNotNull(databaseConnectorException.getCause());
    Assert.assertNotNull(databaseConnectorException.getCause().getMessage());
    Assert.assertEquals(COULD_NOT_CONNECT_TO_DATABASE,
        databaseConnectorException.getCause().getMessage());
  }

  @Test(expected = ConfigurationException.class)
  public void testThrow_ConfigurationException() throws ConfigurationException {
    SQLException sqlException = new SQLException(COULD_NOT_CONNECT_TO_DATABASE);
    throw new ConfigurationException(sqlException);
  }

  @Test
  public void testContent_ConfigurationException() throws ConfigurationException {
    SQLException sqlException = new SQLException(COULD_NOT_CONNECT_TO_DATABASE);
    ConfigurationException confEx = new ConfigurationException(sqlException);
    Assert.assertNotNull(confEx);
    Assert.assertNotNull(confEx.getCause());
    Assert.assertNotNull(confEx.getCause().getMessage());
    Assert.assertEquals(COULD_NOT_CONNECT_TO_DATABASE, confEx.getCause().getMessage());
  }

}
