import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

public class Starter
{
  private static final Logger logger = Logger.getLogger(Starter.class);
	 
  public static void main(String[] args)
    throws InterruptedException, IOException, SQLException
  {
	  runPinger();
  }

private static void runPinger() throws SQLException, IOException,
		InterruptedException {
	logger.debug("Started sucessfully");
    int last_id = 0;
    
    while (true) {
      int poolsize = Integer.parseInt(DBManager.getPrefs().getProperty("poolsize"));
      
      ProcessMap prMap = DBManager.fillMapFromDb(last_id, poolsize);  
      last_id = prMap.getLastId();
      
      JobProcessor printer = new JobProcessor();
      printer.start();
      Connector connector = new Connector(printer);
      connector.start();
      
      logger.debug("hosts.size = " + prMap.getHosts().size());
      logger.debug("poolsize = " + poolsize);
      //last lap
      if (prMap.getHosts().size() == 0) {
    	  last_id = 0;
    	  logger.debug("last_id reseted to zero");
    	  System.runFinalization();
      }      

      LinkedList<PingResult> targets = new LinkedList<PingResult>();
      Iterator<Map.Entry<String, Integer>> it = prMap.getHosts().entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>)it.next();
        PingResult t = new PingResult((String)pairs.getKey(), ((Integer)pairs.getValue()).intValue());
        targets.add(t);
        connector.add(t);
      }

      String waitTime = DBManager.getPrefs().getProperty("wait");
      logger.debug("Wait for everything to finish = " + waitTime + "after id" + last_id);
      Thread.sleep(Integer.parseInt(waitTime));
      connector.shutdown();
      connector.join();

      for (Iterator<PingResult> i = targets.iterator(); i.hasNext(); ) {
        PingResult t = (PingResult)i.next();
        if (!t.isFinished()) {
          t.storeResult();
        }

      }
      
      printer.interrupt();
    }
}
}

