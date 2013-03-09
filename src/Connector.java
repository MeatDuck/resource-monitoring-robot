import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

public class Connector extends Thread {
	private static final int TIMEOUT = 10000;
	private static final Logger logger = Logger.getLogger(Connector.class);
	private Selector sel;
	private JobProcessor printer;
	private LinkedList<PingResult> pending = new LinkedList<PingResult>();

	volatile boolean shutdown = false;

	public Connector(JobProcessor pr) throws IOException {
		this.printer = pr;
		this.sel = Selector.open();
		setName("Connector");
	}

	public void add(PingResult t) {	
		SocketChannel sc = null;
		try {
			sc = SocketChannel.open();			
			sc.configureBlocking(false);
			sc.socket().setSoTimeout(TIMEOUT);
			
			InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(t.getHost()), t.getPort());

			boolean connected = sc.connect(address);
			logger.info("connected to " + t.getHost());
			
			t.setChannel(sc);
			t.setConnectStart(System.currentTimeMillis());

			if (connected) {
				t.setConnectFinish(t.getConnectStart());
				sc.close();
				this.printer.add(t);
			} else {
				synchronized (this.pending) {
					this.pending.add(t);
				}

				this.sel.wakeup();
			}
		} catch (IOException x) {
			logError(t, x);
			tryToClose(sc);
			return;
		} catch (IllegalArgumentException x) {
			t.setFailure(null);
			this.printer.add(t);
			logger.error(x.getMessage(), x);
			tryToClose(sc);
		}
	}

	private void logError(PingResult t, IOException e) {
		t.setFailure(e);
		this.printer.add(t);
		logger.error(e.getMessage(), e);
	}

	public static void tryToClose(SocketChannel sc) {
		if (sc != null) {
			try {
				sc.close();
			} catch (IOException localIOException2) {}
		}
	}

	private void processPendingTargets() throws IOException {
		synchronized (this.pending) {
			while (this.pending.size() > 0) {
				PingResult t = (PingResult) this.pending.removeFirst();
				try {
					t.getChannel().register(this.sel, 8, t);
				} catch (IOException x) {
					t.getChannel().close();
					logError(t, x);
				}
			}
		}
	}

	private void processSelectedKeys() throws IOException {
		for (Iterator<SelectionKey> i = this.sel.selectedKeys().iterator(); i
				.hasNext();) {
			SelectionKey sk = (SelectionKey) i.next();
			i.remove();

			PingResult t = (PingResult) sk.attachment();
			SocketChannel sc = (SocketChannel) sk.channel();
			try {
				if (sc.finishConnect()) {
					sk.cancel();
					t.setConnectFinish(System.currentTimeMillis());
					sc.close();
					this.printer.add(t);
				}
			} catch (IOException x) {
				sc.close();
				logError(t, x);
			}
		}
	}

	public void shutdown() {
		this.shutdown = true;
		this.sel.wakeup();
	}

	public void run() {
		while (true)
			try {
				int n = this.sel.select();
				if (n > 0)
					processSelectedKeys();
					processPendingTargets();
				if (this.shutdown) {
					this.sel.close();
					return;
				}
			} catch (IOException x) {
				logger.error(x.getMessage(), x);
			}
	}
	
	protected void finalize() throws Throwable{
		try {
			if(sel != null && sel.isOpen()) {
				sel.close();
			}
		} catch (IOException e) {}
		super.finalize();
	}
}
