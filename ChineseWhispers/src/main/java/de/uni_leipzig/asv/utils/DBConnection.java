/**
 * author: stefan bordag
 * modified by sven teresniak - teresniak@informatik.uni-leipzig.de
 */
package de.uni_leipzig.asv.utils;

// standard imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

/**
 * This class represents a connection to a DB and can receive and process
 * queries.
 */
public class DBConnection
{
  /**
   * The real connection around which this class is wrapped
   */
  protected Connection connection = null;

  /**
   * The driver used to connect to the DB
   */
  private Object driver = null;

  /**
   * Don't use this
   */
  private DBConnection()
  {
  }


  /**
   * Construct an instance of this Object which can then execute queries on
   * the DB
 * @throws IOWrapperException
   */
  public DBConnection(String URL, String user, String passwd) throws IOWrapperException
  {
    String driver = "org.gjt.mm.mysql.Driver";
    loadDriver(driver);
    try
    {
      System.out.println("Connecting to DB.");
      System.out.println("Installed drivers: ");
      for ( Enumeration enumeration = DriverManager.getDrivers() ; enumeration.hasMoreElements() ; )
      {
        System.out.println("["+enumeration.nextElement().toString()+"]");
      }
      // fix URL
      if ( URL.lastIndexOf("?user=") < 1 )
      {
        URL = URL + "?user=" + user + "&password=" + passwd;
      }

      // go!
      this.connection = DriverManager.getConnection(URL, user, passwd);
    }
    catch (Exception ex)
    {
      System.out.println("Couldn't establish connection to DB ");
      ex.printStackTrace();
      throw new IOWrapperException(ex.getMessage());
    }

  }

  /**
   * Loads the mysql driver or exits
   */
  private void loadDriver(String driver)
  {
    try
    {
      this.driver = Class.forName(driver).newInstance();
      System.out.println(driver.toString());
    }
    catch (Exception e)
    {
      System.out.println("JDBC-Treiber konnte nicht geladen werden.\n");
      e.printStackTrace();
    }
  }

  public boolean isConnected()
  {
    if ( this.connection != null )
    {
      return true;
    }
    return false;
  }


  /**
   * Returns a String array containing the resulting table from the query, null
   * if there was some problem with the query.
 * @throws SQLException
   */
  /*public String[][] getResultsOf(String query, String[] queryArguments) throws IllegalArgumentException
  {
    try
    {
      PreparedStatement q = connection.prepareStatement(query, queryArguments);

      ResultSet set = q.executeQuery();
      //
    }
    catch ( Exception ex )
    {
      ex.printStackTrace();
    }

//    connection.prepareStatement()
    return null;
  }
*/
  
  public ResultSet executeQuery(String query) throws SQLException
  {
    
    ResultSet resultSet = null;
    int columnCount = 0,
        rowCount = 0;


      Statement stat = this.connection.createStatement();
      resultSet = stat.executeQuery(query);
      ResultSetMetaData metaData = resultSet.getMetaData();
//      System.err.println("metadata: "+metaData);
//      columnCount = metaData.getColumnCount();
//      System.out.println("lines got: "+columnCount);
      /*while ( resultSet.next() ) {
        String[] row = new String[columnCount];
        for ( int column = 0; column < columnCount; column++ ){
          row[column] = resultSet.getString(column+1);
        }
        rows.add(row);
      }*/
    


    return resultSet;
  }

  
  public int executeUpdate(String query) throws Exception{
             
     Statement stat = this.connection.createStatement();
     int ret = stat.executeUpdate(query);
     return ret;   
     
  }
  
  
  /**
   * Returns how many time a given word as a number in the DB has been seen
   * or null.
   */
  /*public Integer getAnzForWord(Word word)
  {
    return null;
  }*/

  /**
   * Overridden toString method to get some status information as well.
   */
  public String toString()
  {
    return "DBConnection "+super.toString();
  }



  // very simple method: just gets a cursor to a given query and retunrs it:
  private ResultSet getResultSet(String query){

        ResultSet result = null;
        try{
                Statement stat = this.connection.createStatement();
                result = stat.executeQuery(query);
        }
        catch(SQLException e){
          System.err.println("Query: "+query);
                System.err.println(e.toString());
                e.printStackTrace();
        }

        return result;
  }


  public static void main(String[] args) throws IOWrapperException
  {
    DBConnection con = new DBConnection("jdbc:mysql://aspra16.informatik.uni-leipzig.de/de","eval","rezylana[5]");

  }



}
