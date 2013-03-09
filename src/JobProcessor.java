import java.sql.SQLException;
import java.util.LinkedList;

import org.apache.log4j.Logger;

public class JobProcessor extends Thread {
	private static final Logger logger = Logger.getLogger(JobProcessor.class);
	private LinkedList<PingResult> pending = new LinkedList<PingResult>();

	public JobProcessor() {
		setName("Printer");
		setDaemon(true);
	}

	void add(PingResult t) {
		synchronized (this.pending) {
			this.pending.add(t);
			this.pending.notify();
		}
	}

	public void run() {
		try {
			while (true) {
				PingResult t = null;
				synchronized (this.pending) {
					while (this.pending.size() == 0){
						this.pending.wait();
					}
					t = (PingResult) this.pending.removeFirst();
				}
				t.storeResult();
			}
		} catch (InterruptedException x) {
			logger.error(x.getMessage(), x);
			return;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
