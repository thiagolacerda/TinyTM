package TinyTM.contention;

import java.util.Random;

import TinyTM.Transaction;

public class PriorityManager extends ContentionManager {

    private static final int WAIT_DELAY = 1024;
    Random random = new Random();

	@Override
	public void resolve(Transaction me, Transaction other) {
		if (me.getTimestamp() < other.getTimestamp())
			other.abort();
		else {
			try {
                Thread.sleep(random.nextInt(WAIT_DELAY));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
		}
	}

}
