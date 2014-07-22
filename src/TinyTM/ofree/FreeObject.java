/*
 * AtomicObject.java
 *
 * Created on January 17, 2007, 7:29 PM
 *
 * From "The Art of Multiprocessor Programming",
 * by Maurice Herlihy and Nir Shavit.
 *
 * This work is licensed under a Creative Commons Attribution-Share Alike 3.0 United States License.
 * http://i.creativecommons.org/l/by-sa/3.0/us/88x31.png
 */

package TinyTM.ofree;

/**
 * Encapsulates transactional synchronization for obstruction-free objects.
 * @author Maurice Herlihy
 */

import TinyTM.Copyable;
import TinyTM.Transaction;
import TinyTM.contention.ContentionManager;
import TinyTM.exceptions.AbortedException;
import TinyTM.exceptions.PanicException;

import java.util.concurrent.atomic.AtomicReference;

public class FreeObject<T extends Copyable<T>> extends TinyTM.AtomicObject<T> {
    AtomicReference<Locator> start;
    private Transaction.Status lastStatus = Transaction.Status.ACTIVE;

    public FreeObject(T init) {
        super(init);
        start = new AtomicReference<Locator>(new Locator(init));
    }

    public T openWrite() {
        Transaction me = Transaction.getLocal();
        switch (me.getStatus()) {
            case COMMITTED:
                return openSequential();
            case ABORTED:
                throw new AbortedException();
            case ACTIVE:
                Locator locator = start.get();
                if (locator.owner == me) {

                    // If this object was previously opened on read mode, then we just have the old version.
                    if (locator.newVersion == null) {
                        // Copy oldVersion to newVersion
                        try {
                            locator.newVersion = myClass.newInstance();
                        } catch (Exception e) {
                            throw new PanicException(e);
                        }

                        locator.oldVersion.copyTo(locator.newVersion);
                    }

                    return locator.newVersion;
                }

                Locator newLocator = new Locator(me);
                while (!Thread.currentThread().isInterrupted()) {
                    Locator oldLocator = start.get();
                    Transaction writer = oldLocator.owner;
                    switch (writer.getStatus()) {
                        case COMMITTED:
                            newLocator.oldVersion = (oldLocator.newVersion == null) ? oldLocator.oldVersion : oldLocator.newVersion;
                            break;
                        case ABORTED:
                            newLocator.oldVersion = oldLocator.oldVersion;
                            break;
                        case ACTIVE:
                            if (lastStatus != Transaction.Status.ABORTED)
                                me.setTimestamp();
                            ContentionManager.getLocal().resolve(me, writer);
                            continue;
                    }
                    try {
                        newLocator.newVersion = myClass.newInstance();
                    } catch (Exception ex) {
                        throw new PanicException(ex);
                    }
                    newLocator.oldVersion.copyTo(newLocator.newVersion);
                    if (start.compareAndSet(oldLocator, newLocator)) {
                        ContentionManager.getLocal().setPriority();
                        return newLocator.newVersion;
                    }
                }
                me.abort(); // time's up
                throw new AbortedException();
            default:
                throw new PanicException("Unexpected transaction state");
        }
    }

    private T openSequential() {
        Locator locator = start.get();

        // If a different transaction is holding the object, then we invoke ContentionManager to resolve the conflict.
        while(locator.owner.getStatus() == Transaction.Status.ACTIVE) {
            ContentionManager.getLocal().resolve(Transaction.getLocal(), locator.owner);
        }

        switch (locator.owner.getStatus()) {
            case COMMITTED:
                return (locator.newVersion == null) ? locator.oldVersion : locator.newVersion;
            case ABORTED:
                return locator.oldVersion;
            default:
                throw new PanicException("Active/Inactitive transaction conflict");
        }
    }

    public T openRead() {
        Transaction me = Transaction.getLocal();
        switch (me.getStatus()) {
            case COMMITTED:
                return openSequential();
            case ABORTED:
                throw new AbortedException();
            case ACTIVE:
                Locator locator = start.get();
                if (locator.owner == me) {
                    // If this object was previously opened on read mode, then we just have the old version.
                    return (locator.newVersion == null) ? locator.oldVersion : locator.newVersion;
                }
                Locator newLocator = new Locator(me);
                while (!Thread.currentThread().isInterrupted()) {
                    Locator oldLocator = start.get();
                    Transaction writer = oldLocator.owner;
                    switch (writer.getStatus()) {
                        case COMMITTED:
                            newLocator.oldVersion = (oldLocator.newVersion == null) ? oldLocator.oldVersion : oldLocator.newVersion;
                            break;
                        case ABORTED:
                            newLocator.oldVersion = oldLocator.oldVersion;
                            break;
                        case ACTIVE:
                            if (lastStatus != Transaction.Status.ABORTED)
                                me.setTimestamp();
                            ContentionManager.getLocal().resolve(me, writer);
                            continue;
                    }

                    if (start.compareAndSet(oldLocator, newLocator)) {
                        ContentionManager.getLocal().setPriority();
                        return newLocator.oldVersion;
                    }
                }
                me.abort(); // time's up
                throw new AbortedException();
            default:
                throw new PanicException("Unexpected transaction state");
        }
    }

    public boolean validate() {
        lastStatus = Transaction.getLocal().getStatus();
        switch (Transaction.getLocal().getStatus()) {
            case COMMITTED:
                return true;
            case ABORTED:
                return false;
            case ACTIVE:
                return true;
            default:
                throw new PanicException("Unexpected Transaction state");
        }
    }

    private class Locator {
        Transaction owner;
        T oldVersion;
        T newVersion;

        Locator() {
            owner = Transaction.COMMITTED;
        }

        Locator(T version) {
            this();
            newVersion = version;
        }

        Locator(Transaction me) {
            owner = me;
        }
    }

}
