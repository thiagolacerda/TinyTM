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

    private long timestamp;
    private boolean waiting;

    static ThreadLocal<Transaction> local = new ThreadLocal<Transaction>() {
        @Override
        protected Transaction initialValue() {
            return new Transaction(Status.COMMITTED);
        }
    };
    private final AtomicReference<Status> status;

    public Transaction() {
        status = new AtomicReference<Status>(Status.ACTIVE);
    }

    private Transaction(Transaction.Status myStatus) {
        status = new AtomicReference<Status>(myStatus);
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

    public boolean commit() {
        return status.compareAndSet(Status.ACTIVE, Status.COMMITTED);
    }

    public boolean abort() {
        return status.compareAndSet(Status.ACTIVE, Status.ABORTED);
    }

    public void setTimestamp() {
        timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setWaiting(boolean value) {
        waiting = value;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public enum Status {ABORTED, ACTIVE, COMMITTED}
}
