import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;


public class DBManager
{
  private static final Logger logger = Logger.getLogger(DBManager.class);
  private static Connection instance;
  private static Properties prefs = null;

  public static Properties getPrefs() {
    if (prefs == null) {
      loadSettings();
    }
    return prefs;
  }

  public static PreparedStatement prepareStatement(String sql)
  {
    if (instance == null) {
      try {
        Class.forName("com.mysql.jdbc.Driver");
      } catch (ClassNotFoundException e) {
        logger.error("Can't find jdbc: " + e.getMessage(), e);
      }
      String url = "jdbc:mysql://" + prefs.getProperty("host") + ":" + prefs.getProperty("port") + "/" + prefs.getProperty("base");
      try {
        instance = DriverManager.getConnection(url, prefs.getProperty("login"), prefs.getProperty("pass"));
        logger.debug("Sucesfully counected to db");
      } catch (SQLException e) {
        logger.error("MySQL connection error: " + e.getMessage(), e);
      }
    }
    PreparedStatement statement = null;
    
	try {
		statement = instance.prepareStatement(sql);
	} catch (SQLException e) {
		logger.error("MySQL connection error: " + e.getMessage(), e);
		System.exit(9);
	}    
    return statement;
  }

  private static void loadSettings() {
    String filename = System.getProperty("user.dir") + System.getProperty("file.separator") + "config.properties";
    prefs = new Properties();
    try
    {
      FileInputStream st = new FileInputStream(filename);
      prefs.load(st);
      st.close();
    } catch (FileNotFoundException e1) {
      logger.error("Can't load ini file: " + e1.getMessage(), e1);
      System.exit(0);
    } catch (IOException e1) {
      logger.error("Can't load ini file" + e1.getMessage(), e1);
      System.exit(0);
    }
    logger.debug("Sucesfully loaded ini file");
  }

	public static int getCountOfSites() throws SQLException {
		String query2 = "SELECT count(*) count from resource where ison = true and website not in (select website from forbidden) ";
	      PreparedStatement stmt = DBManager.prepareStatement(query2);      
	      ResultSet rs2 = stmt.executeQuery();
	      rs2.next();
	      int countOfSites = rs2.getInt(1);
	      
	      logger.debug("countOfSites =  " + countOfSites);
	      if (countOfSites == 0) {
	          logger.error("Resource table is empty");
	      }
	      rs2.close();
	      stmt.close();
	      
		return countOfSites;
	}

	public static ProcessMap fillMapFromDb(int last_id, int poolsize) throws SQLException {
		ProcessMap prMap = new ProcessMap(new HashMap<String, Integer>(), last_id);
	    String query = "SELECT * from resource where ison = true and id_resource > ? and website not in (select website from forbidden)  ORDER BY id_resource limit ?";
	    PreparedStatement stmt2 = DBManager.prepareStatement(query);   
	    stmt2.setInt(1, prMap.getLastId());
	    stmt2.setInt(2, poolsize);
	    logger.debug("Execute query: " + query);
	    ResultSet  rs = stmt2.executeQuery();	    
	    while (rs.next()) {
	        String website = rs.getString("website");
	        int port = rs.getInt("port");
	        prMap.getHosts().put(website, Integer.valueOf(port));
	        prMap.setLastId(rs.getInt("id_resource"));
	        logger.debug("Param website = " + website);
	        logger.debug("Param port = " + port);
	        logger.debug("Param last_id = " + prMap.getLastId());
	    }	 
	    rs.close();
	    stmt2.close();
	    
		return prMap;
	}
	
	public static void resetConnection(){
		if (instance != null) {
			try {
				instance.close();
				instance = null;
			} catch (SQLException e) {
				logger.error("Can't close connection", e);				
			}
		}
	}
}

