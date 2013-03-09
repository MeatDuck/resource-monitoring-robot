import java.nio.channels.SocketChannel;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class PingResult {
	private static final String OK_SQL = "update resource set `pingtime` = ?, `last_access` = now(), `sucess` = `sucess` + 1, `die` = 1 WHERE website = ? and port = ?";
	private static final String FAILED_SQL = "update resource set `pingtime` = ?, `last_access` = now(), `fail` = `fail` + 1, `die` = 0 WHERE website = ? and port = ? ";

	private static final Logger logger = Logger.getLogger(PingResult.class);

	
	private String host;
	private int port;
	private SocketChannel channel;
	private Exception failure;
	private long connectStart;
	private long connectFinish = 0L;
	private boolean finished = false;
	
	public PingResult(String host, int port) {
			this.host = host;
			this.port = port;
	}

	public void storeResult() throws SQLException {
		Integer timeDiff = 0;
		if (this.connectFinish > 0L) {
			timeDiff = Math.round(this.connectFinish - this.connectStart);
		} else {
			failure = new Exception("Connect finish time -  connect start time <= 0");
			logger.debug(failure);
		}

		
		if (failure != null) {
			PreparedStatement stmt = DBManager.prepareStatement(FAILED_SQL);
			prepareParams(timeDiff, stmt);
			stmt.executeUpdate();
			stmt.close();
			logger.info("Stored info " + this.host + ":" + this.port + " failed");
		} else {
				PreparedStatement stmt = DBManager.prepareStatement(OK_SQL);
				prepareParams(timeDiff, stmt);
				stmt.executeUpdate();
				stmt.close();
				logger.info("Stored info " + this.host + ":" + this.port + " alive");
		}

		setFinished(true);
	}

	private void prepareParams(Integer timeDiff, PreparedStatement stmt)
			throws SQLException {
		stmt.setInt(1, timeDiff);
		stmt.setString(2, this.host);
		stmt.setInt(3, this.port);
	}
		
	public SocketChannel getChannel() {
		return channel;
	}

	public void setChannel(SocketChannel channel) {
		this.channel = channel;
	}

	public Exception getFailure() {
		return failure;
	}

	public void setFailure(Exception failure) {
		this.failure = failure;
	}

	public long getConnectStart() {
		return connectStart;
	}

	public void setConnectStart(long connectStart) {
		this.connectStart = connectStart;
	}

	public long getConnectFinish() {
		return connectFinish;
	}

	public void setConnectFinish(long connectFinish) {
		this.connectFinish = connectFinish;
	}
	
	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public int getPort() {
		return this.port;
	}

	public String getHost() {
		return this.host;
	}
	
	protected void finalize() throws Throwable{
		Connector.tryToClose(channel);
		super.finalize();		
	}
}
