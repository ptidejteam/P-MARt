/*BEGIN_COPYRIGHT_BLOCK
 *
 * This file is a part of DrJava. Current versions of this project are available
 * at http://sourceforge.net/projects/drjava
 *
 * Copyright (C) 2001-2002 JavaPLT group at Rice University (javaplt@rice.edu)
 * 
 * DrJava is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrJava is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * or see http://www.gnu.org/licenses/gpl.html
 *
 * In addition, as a special exception, the JavaPLT group at Rice University
 * (javaplt@rice.edu) gives permission to link the code of DrJava with
 * the classes in the gj.util package, even if they are provided in binary-only
 * form, and distribute linked combinations including the DrJava and the
 * gj.util package. You must obey the GNU General Public License in all
 * respects for all of the code used other than these classes in the gj.util
 * package: Dictionary, HashtableEntry, ValueEnumerator, Enumeration,
 * KeyEnumerator, Vector, Hashtable, Stack, VectorEnumerator.
 *
 * If you modify this file, you may extend this exception to your version of the
 * file, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version. (However, the
 * present version of DrJava depends on these classes, so you'd want to
 * remove the dependency first!)
 *
END_COPYRIGHT_BLOCK*/

package edu.rice.cs.drjava.model.definitions.reducedmodel;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * A list class with some extra features.
 * Allows multiple iterators to make modifications to the same list
 * without failing like the iterators for java.util.*List.
 * @version $Id: ModelList.java,v 1.1 2005/08/05 12:45:57 guehene Exp $
 */
class ModelList<T> {
  private Node<T> _head;
  private Node<T> _tail;
  /** keep track of length for constant time length lookup */
  private int _length;
  /** a set of objects that can trigger and listen for updates to the list */
  private Set _listeners;
  
  /**
   * Constructor.
   * Initializes the head and tail nodes, as well as the listener table
   * and the length variable.
   */
  ModelList() {
    // This node is the only node that exists in an empty list.
    // If an Iterator points to this node, the iterator is considered
    // to be in "initial position."
    _head = new Node<T>();
    _tail = new Node<T>();
    _head.pred = null;
    _head.succ = _tail;
    _tail.pred = _head;
    _tail.succ = null;
    _length = 0;
    _listeners = new HashSet();
  }
  
  /**
   * Insert an item before a certain node in the list.
   * Can never be called on head node.
   */
  private void insert(Node<T> point, T item) {
    Node<T> ins = new Node<T>(item, point.pred, point);
    point.pred.succ = ins;
    point.pred = ins;
    _length++;
  }
   
  public void insertFront(T item) {
    Iterator it = new Iterator();
    it.insert(item);
    it.dispose();
  }
  
  /**
   * Remove a node from the list.
   * Can't remove head or tail node - exception thrown.
   */
  private void remove(Node<T> point) {
    if ((point == _head) || (point == _tail))
      throw new RuntimeException("Can't remove head.");
    else
    {
      point.succ.pred = point.pred;
      point.pred.succ = point.succ;
      _length--;
    }
  }
  
  private void addListener(Object thing) {
    this._listeners.add(thing);
  }
  
  private void removeListener(Object thing) {
    this._listeners.remove(thing);
  }

  public int listenerCount() {
    return _listeners.size();
  }
  /**
   * Returns true if the list is empty.
   */
  public boolean isEmpty() {
    return (_head.succ == _tail);
  }
  
  public int length() {
    return _length;
  }
  
  /**
   * Create a new iterator for this list.  The constructor for the
   * iterator adds itself to the list's listeners.  The iterator 
   * must be notified of changes so it does not become out-of-date.
   */
  public Iterator getIterator() {
    return new Iterator();
  }
  
  
  /**
   * A node class for the list.
   * Each node has a successor and predecessor, which is also a node
   * as well as an item of type T.
   * We keep it private inside this class so Node is never shown to
   * the outside world to wreak havoc upon the list.
   */
  private static class Node<T> {
    Node<T> pred;
    Node<T> succ;
    private T _item;
    
    Node() {
      _item = null;
      pred = this;
      succ = this;
    }
    
    Node(T item, Node<T> previous, Node<T> successor) {
      _item = item;
      pred = previous;
      succ = successor;
    }
    
    T getItem() {
      return _item;
    }
  }
  
  /**
   * Iterators for model list.
   * The iterators are intimately coupled with the ModelList to which they
   * belong.  They are the only public interface for manipulating
   * ModelList.  The iterators are also fail-safe with regards to 
   * manipulation of the same list, although probably not thread-safe.
   */
  class Iterator {
    private Node<T> _point;
    private int _pos;
    
    /**
     * Constructor.
     * Initializes an iterator to point to its list's head.
     */
    public Iterator() {
      _point = ModelList.this._head;
      _pos = 0;
      ModelList.this.addListener(this);
    }
    
    /**
     * Copy constructor.
     * Creates a new iterator with the same values as the progenitor.
     * Adds it to the list's set of listeners.
     */
    public Iterator(Iterator iter) {
      _point = iter._point;
      _pos = iter._pos;
      ModelList.this.addListener(this);
    }
    
    public Iterator copy() {
      return new Iterator(this);
    }
    
    /**
     * an equals test
     */
    public boolean eq(Object thing) {
      return this._point == ((Iterator)(thing))._point;
    }
    
    /**
     * Force this iterator to take the values of the given iterator.
     */
    public void setTo(Iterator it) {
      this._point = it._point;
      this._pos = it._pos;
    }
    
    /**
     * Disposes of an iterator by removing it from the list's set of
     * listeners.  When an iterator is no longer necessary, it
     * should be disposed of.  Otherwise, there will be memory leaks
     * because the listener set of the list provides a root reference
     * for the duration of the list's existence.  What this means is that
     * unless an iterator is disposed of, it will continue to exist even
     * after garbage collection as long as the list itself is not
     * garbage collected.
     */
    public void dispose() {
      ModelList.this.removeListener(this);
    }
    
    /**
     * Return true if we're pointing at the head.
     */
    public boolean atStart() {
      return (_point == ModelList.this._head);
    }
    
    /**
     * Return true if we're pointing at the tail.
     */
    public boolean atEnd() {
      return (_point == ModelList.this._tail);
    }
    
    /**
     * Return true if we're pointing at the node after the head.
     */
    public boolean atFirstItem() {
      return (_point.pred == ModelList.this._head);
    }
    
    /**
     * Return true if we're pointing at the node before the tail.
     */
    public boolean atLastItem() {
      return (_point.succ == ModelList.this._tail);
    }
    
    /**
     * Return the item associated with the current node.
     */
    public T current() {
      if (atStart()) {
        throw new RuntimeException("Attempt to call current on an " +
                                   "iterator in the initial position");
      }
      else if (atEnd()) {
        throw new RuntimeException("Attempt to call current on an " +
                                   "iterator in the final position");
      }
      else {
        return _point.getItem();
      }
    }
    
    /**
     * Return the item associated with the node before the current node.
     */
    public T prevItem() {
      if (atFirstItem() || atStart() || ModelList.this.isEmpty()) {
        throw new RuntimeException("No more previous items.");
      }
      else {
        return _point.pred.getItem();
      }
    }
    
    /**
     * Return the item associated with the node after the current node.
     */
    public T nextItem() {
      if (atLastItem() || atEnd() || ModelList.this.isEmpty()) {
        throw new RuntimeException("No more following items.");
      }
      else {
        return _point.succ.getItem();
      }
    }
    
    /**
     * Insert an item before the current item.
     * If at the containing list's head, we need to move to the next node
     * to perform the insert properly.  Otherwise, we'll get a null pointer
     * exception because the function will try to insert the new item
     * before the head.
     *
     * Ends pointing to inserted item.
     */
    public void insert(T item) {
      //so as not to insert at head
      if (this.atStart()) {
        next();
      }
      ModelList.this.insert(_point, item);
      _point = _point.pred; //puts pointer on inserted item
      notifyOfInsert(_pos);
      
      //because notifyOfInsert will change the position of this iterator
      //we must change it back.
      _pos -= 1;
    }
    
    /**
     * Remove the current item from the list.
     * Ends pointing to the node following the removed node.
     * Throws exception if performed atStart() or atEnd().
     */
    public void remove() {
      Node<T> tempNode = _point.succ;
      ModelList.this.remove(_point);
      _point = tempNode;
      notifyOfRemove(_pos, _point);
    }
    
    /**
     * Move to the previous node.
     * Throws exception atStart().
     */
    public void prev() {
      if (atStart()) {
        throw new RuntimeException("Can't cross list boundary.");
      }
      _point = _point.pred;
      _pos--;
    }
    
    /**
     * Move to the next node.
     * Throws exception atEnd().
     */
    public void next() {
      if (atEnd()) {
        throw new RuntimeException("Can't cross list boundary.");
      }
      _point = _point.succ;
      _pos++;
    }
    
    /**
     * Delete all nodes between the current position of this and the
     * current position of the given iterator.
     *
     * 1)Two iterators pointing to same node: do nothing
     * 2)Iterator 2 is before iterator 1    : remove between iterator 2 and
     *                                       iterator 1
     * 3)Iterator 1 is before iterator 2    : remove between iterator 1 and
     *                                       iterator 2
     *
     *D oes not remove points iterators point to.
     */
    public void collapse(Iterator iter) {
      int leftPos;
      int rightPos;
      Node<T> rightPoint;
      
      if (this._pos > iter._pos) {
        leftPos = iter._pos;
        rightPos = this._pos;
        rightPoint = this._point;
        
        this._point.pred = iter._point;
        iter._point.succ = this._point;
        //determine new length
        ModelList.this._length -= this._pos - iter._pos - 1;
        notifyOfCollapse(leftPos, rightPos, rightPoint);
      }
      else if (this._pos < iter._pos) {
        leftPos = this._pos;
        rightPos = iter._pos;
        rightPoint = iter._point;
        
        iter._point.pred = this._point;
        this._point.succ = iter._point;
        
        ModelList.this._length -= iter._pos - this._pos - 1;
        notifyOfCollapse(leftPos, rightPos, rightPoint);
      }
      else { // this._pos == iter._pos
      }
    }
    
    /**
     * When an iterator inserts an item, it notifies other iterators
     * in the set of listeners so they can stay updated.
     */
    private void notifyOfInsert(int pos) {
      java.util.Iterator iter = ModelList.this._listeners.iterator();
      while (iter.hasNext()) {
        Iterator next = (Iterator)iter.next();
        if ( next._pos < pos ) {
          // do nothing
        }
        else { // ( next._pos == pos ) || next._pos > pos
          next._pos += 1;
        }
      }
    }
    
    /**
     * When an iterator removes an item, it notifies other iterators
     * in the set of listeners so they can stay updated.
     */
    private void notifyOfRemove(int pos, Node<T> point) {
      java.util.Iterator iter = ModelList.this._listeners.iterator();
      while (iter.hasNext()) {
        Iterator next = (Iterator)iter.next();
        if ( next._pos < pos ) {
          // do nothing
        }
        else if ( next._pos == pos ) {
          next._point = point;
        }
        else { // next._pos > pos
          next._pos -= 1;
        }
      }
    }
    
    /**
     * When an iterator collapses part of the list, it notifies other iterators
     * in the set of listeners so they can stay updated.
     */
    private void notifyOfCollapse(int leftPos, int rightPos, Node<T> rightPoint) {
      java.util.Iterator iter = ModelList.this._listeners.iterator();
      while (iter.hasNext()) {
        Iterator next = (Iterator)iter.next();
        if ( next._pos <= leftPos ) {
          // do nothing
        }
        else if (( next._pos > leftPos ) && ( next._pos <= rightPos )) {
          next._pos = leftPos + 1;
          next._point = rightPoint;
        }
        else { // next._pos > rightPos
          next._pos -= (rightPos - leftPos - 1);
        }
      }
    }
  }
}
