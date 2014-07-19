/*
 * AtomicNode.java
 *
 * Created on February 6, 2007, 9:08 PM
 *
 * This work is licensed under a Creative Commons Attribution-Share Alike 3.0 United States License.
 * http://i.creativecommons.org/l/by-sa/3.0/us/88x31.png
 *
 */

package TinyTM.list.ofree;

import TinyTM.AtomicObject;
import TinyTM.exceptions.AbortedException;
import TinyTM.list.INode;
import TinyTM.list.SNode;
import TinyTM.ofree.FreeObject;

/**
 * Atomic list node using locking AtomicObject Implementation
 *
 * @param <T> type
 * @author Maurice Herlihy
 */
public class TNode<T> implements INode<T> {
    AtomicObject<SNode<T>> atomic;

    public TNode(T v) {
        atomic = new FreeObject<SNode<T>>(new SNode<T>(v));
    }

    public TNode(int key, T v) {
        atomic = new FreeObject<SNode<T>>(new SNode<T>(key, v));
    }

    public int getKey() {
        int value = atomic.openRead().getKey();
        if (!atomic.validate())
            throw new AbortedException();
        return value;
    }

    public void setKey(int value) {
        atomic.openWrite().setKey(value);
    }

    public T getItem() {
        T value = atomic.openRead().getItem();
        if (!atomic.validate())
            throw new AbortedException();
        return value;
    }

    public void setItem(T value) {
        atomic.openWrite().setItem(value);
    }

    public INode<T> getNext() {
        INode<T> value = atomic.openRead().getNext();
        if (!atomic.validate())
            throw new AbortedException();
        return value;
    }

    public void setNext(INode<T> value) {
        atomic.openWrite().setNext(value);
    }
}
