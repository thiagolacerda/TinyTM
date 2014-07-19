/*
 * ContentionManager.java
 *
 * Created on January 7, 2007, 6:46 PM
 *
 * From "The Art of Multiprocessor Programming",
 * by Maurice Herlihy and Nir Shavit.
 *
 * This work is licensed under a Creative Commons Attribution-Share Alike 3.0 United States License.
 * http://i.creativecommons.org/l/by-sa/3.0/us/88x31.png
 */

package TinyTM.contention;

import TinyTM.Defaults;
import TinyTM.Transaction;
import TinyTM.exceptions.PanicException;

/**
 * Contention Manager Interface for TinyTM
 * <p/>
 * From "The Art of Multiprocessor Programming",
 * by Maurice Herlihy and Nir Shavit.
 * <p/>
 * This work is licensed under a Creative Commons Attribution-Share Alike 3.0 United States License.
 * http://i.creativecommons.org/l/by-sa/3.0/us/88x31.png
 *
 * @author Maurice Herlihy
 */
public abstract class ContentionManager {

    static ThreadLocal<ContentionManager> local = new ThreadLocal<ContentionManager>() {

        @Override
        protected ContentionManager initialValue() {
            try {
                return (ContentionManager) Defaults.MANAGER.newInstance();
            } catch (Exception ex) {
                throw new PanicException(ex);
            }
        }
    };

    public static ContentionManager getLocal() {
        return local.get();
    }

    public static void setLocal(ContentionManager m) {
        local.set(m);
    }

    public abstract void resolve(Transaction me, Transaction other);
}