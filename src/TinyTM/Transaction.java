/*
 * Transaction.java
 *
 * Created on January 3, 2007, 8:44 PM
 *
 * From "The Art of Multiprocessor Programming",
 * by Maurice Herlihy and Nir Shavit.
 *
 * This work is licensed under a Creative Commons Attribution-Share Alike 3.0 United States License.
 * http://i.creativecommons.org/l/by-sa/3.0/us/88x31.png
 */

package TinyTM;

import java.util.concurrent.atomic.AtomicReference;

public class Transaction {
    public static final Transaction COMMITTED = new Transaction(Status.COMMITTED);

    static ThreadLocal<Transaction> local = new ThreadLocal<Transaction>() {
        @Override
        protected Transaction initialValue() {
            return new Transaction(Status.COMMITTED);
        }
    };

    private final AtomicReference<Status> status;
    private final long timestamp;

    public Transaction() {
        this(Status.ACTIVE, System.currentTimeMillis());
    }

    public Transaction(Transaction base) {
        this(Status.ACTIVE, base.getTimestamp());
    }

    private Transaction(Transaction.Status myStatus) {
        this(myStatus, System.currentTimeMillis());
    }

    private Transaction(Transaction.Status myStatus, long myTimestamp) {
        status = new AtomicReference<Status>(myStatus);
        timestamp = myTimestamp;
    }

    public static Transaction getLocal() {
        return local.get();
    }

    public static void setLocal(Transaction transaction) {
        local.set(transaction);
    }

    public Status getStatus() {
        return status.get();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean commit() {
        return status.compareAndSet(Status.ACTIVE, Status.COMMITTED);
    }

    public boolean abort() {
        return status.compareAndSet(Status.ACTIVE, Status.ABORTED);
    }

    public enum Status {ABORTED, ACTIVE, COMMITTED}
}
