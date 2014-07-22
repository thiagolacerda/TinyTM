package TinyTM.contention;

import java.util.Random;

import TinyTM.Transaction;

public class GreedyManager extends ContentionManager {

	private static final int WAIT_DELAY = 1024;
    Random random = new Random();

	@Override
	public void resolve(Transaction me, Transaction other) {
		if (me.getTimestamp() < other.getTimestamp() || other.isWaiting())
			other.abort();
		else {
			try {
				me.setWaiting(true);
				Thread.sleep(random.nextInt(WAIT_DELAY));
	            me.setWaiting(false);
			} catch (InterruptedException ex) {
				me.setWaiting(false);
			}
		}
	}

}
