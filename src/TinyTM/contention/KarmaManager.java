package TinyTM.contention;

import TinyTM.Transaction;

public class KarmaManager extends ContentionManager {

	@Override
	public void resolve(Transaction me, Transaction other) {
		if (me.getContentionManager().getPriority() > other.getContentionManager().getPriority())
			other.abort();
	}

}
