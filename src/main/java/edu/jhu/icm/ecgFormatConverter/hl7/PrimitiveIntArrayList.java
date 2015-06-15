package edu.jhu.icm.ecgFormatConverter.hl7;
/*
Copyright 2015 Johns Hopkins University Institute for Computational Medicine

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import bak.pcj.list.IntArrayList;

/**
 * @author cyang
 * Apr 17, 2006
 */
public class PrimitiveIntArrayList implements List {
    static Logger logger = Logger.getLogger(PrimitiveIntArrayList.class.getName());
    
    private IntArrayList _ial;
    
    public PrimitiveIntArrayList(int[] intArray){
        this._ial =new IntArrayList(intArray);      
    }
    /* (non-Javadoc)
     * @see java.util.Collection#size()
     */
    public int size() {
        return _ial.size();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    public boolean isEmpty() {
        
        return _ial.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        logger.error("not implemented.");
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    public Iterator iterator() {
        logger.error("not implemented.");
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray() {
        logger.error("not implemented.");
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    public Object[] toArray(Object[] a) {
        logger.error("not implemented.");
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public boolean add(Object o) {
        logger.error("not implemented.");
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        logger.error("not implemented.");
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection c) {
        logger.error("not implemented.");
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection c) {
        logger.error("not implemented.");
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.List#addAll(int, java.util.Collection)
     */
    public boolean addAll(int index, Collection c) {
        logger.error("not implemented.");
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection c) {
        logger.error("not implemented.");
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection c) {
        logger.error("not implemented.");
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear() {
        logger.error("not implemented.");

    }

    /* (non-Javadoc)
     * @see java.util.List#get(int)
     */
    public Object get(int index) {
        return new Integer(_ial.get(index));
    }

    /* (non-Javadoc)
     * @see java.util.List#set(int, java.lang.Object)
     */
    public Object set(int index, Object element) {
        logger.error("not implemented.");
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.List#add(int, java.lang.Object)
     */
    public void add(int index, Object element) {
        logger.error("not implemented.");

    }

    /* (non-Javadoc)
     * @see java.util.List#remove(int)
     */
    public Object remove(int index) {
        logger.error("not implemented.");
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.List#indexOf(java.lang.Object)
     */
    public int indexOf(Object o) {
        logger.error("not implemented.");
        return 0;
    }

    /* (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    public int lastIndexOf(Object o) {
        logger.error("not implemented.");
        return 0;
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator()
     */
    public ListIterator listIterator() {
        logger.error("not implemented.");
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.List#listIterator(int)
     */
    public ListIterator listIterator(int index) {
        logger.error("not implemented.");
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.List#subList(int, int)
     */
    public List subList(int fromIndex, int toIndex) {
        logger.error("not implemented.");
        return null;
    }
}