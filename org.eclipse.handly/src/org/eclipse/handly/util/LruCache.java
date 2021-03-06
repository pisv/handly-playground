/*******************************************************************************
 * Copyright (c) 2018 1C-Soft LLC.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Vladimir Piskarev (1C) - initial API and implementation
 *******************************************************************************/
package org.eclipse.handly.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An LRU cache. Entries are ordered in the cache from most recently accessed
 * to least recently accessed. When a cache entry is accessed via {@link
 * #get(Object) get} or {@link #put(Object, Object) put} methods, it is moved
 * to the most recently used position in the cache. No other public methods
 * generate entry accesses.
 */
public class LruCache<K, V>
{
    private Map<K, Entry<K, V>> map = new HashMap<>();
    private Entry<K, V> head, tail;

    /**
     * Returns the size of this cache.
     *
     * @return the size of the cache
     */
    public final int size()
    {
        return map.size();
    }

    /**
     * Returns whether this cache is empty.
     *
     * @return <code>true</code> if the cache is empty,
     *  and <code>false</code> otherwise
     */
    public final boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Returns the corresponding value for the given key and moves the
     * corresponding entry to the most recently used position in this cache.
     * If the cache contains no value for the key, <code>null</code>
     * is returned.
     *
     * @param key the key whose corresponding value is to be returned
     * @return the corresponding value for the given key, or
     *  <code>null</code> if the cache contains no value for the key
     */
    public final V get(Object key)
    {
        Entry<K, V> entry = map.get(key);
        if (entry == null)
            return null;
        moveToMru(entry);
        return entry.value;
    }

    /**
     * Returns the corresponding value for the given key without disturbing
     * cache ordering, or <code>null</code> if this cache contains no value
     * for the key.
     *
     * @param key the key whose corresponding value is to be returned
     * @return the corresponding value for the given key, or
     *  <code>null</code> if the cache contains no value for the key
     */
    public final V peek(Object key)
    {
        Entry<K, V> entry = map.get(key);
        if (entry == null)
            return null;
        return entry.value;
    }

    /**
     * Caches the given value for the given key and moves the corresponding
     * entry to the most recently used position in this cache. Returns the
     * previous value of the updated cache entry, or <code>null</code>
     * if the cache contained no value for the key.
     *
     * @param key the key for which the given value is to be cached
     *  (not <code>null</code>)
     * @param value the value to be cached for the given key
     *  (not <code>null</code>)
     * @return the previous value of the updated cache entry, or
     *  <code>null</code> if the cache contained no value for the key
     */
    public final V put(K key, V value)
    {
        if (key == null)
            throw new IllegalArgumentException();
        if (value == null)
            throw new IllegalArgumentException();
        Entry<K, V> entry = map.get(key);
        if (entry != null)
        {
            V oldValue = entry.value;
            update(entry, value);
            return oldValue;
        }
        add(newEntry(key, value));
        return null;
    }

    /**
     * Removes the cache entry for the given key if it is present.
     * Returns the value of the removed cache entry, or <code>null</code>
     * if this cache contained no value for the key.
     *
     * @param key the key whose entry is to be removed from the cache
     * @return the value of the removed cache entry, or <code>null</code>
     *  if the cache contained no value for the key
     */
    public final V remove(Object key)
    {
        Entry<K, V> entry = map.get(key);
        if (entry == null)
            return null;
        V oldValue = entry.value;
        remove(entry);
        return oldValue;
    }

    /**
     * Removes all entries from this cache.
     */
    public void clear()
    {
        map.clear();
        head = tail = null;
    }

    /**
     * Returns a snapshot of the current contents of this cache,
     * ordered from most recently accessed to least recently accessed.
     *
     * @return a snapshot of the current contents of the cache
     *  (never <code>null</code>)
     */
    public final Map<K, V> snapshot()
    {
        Map<K, V> snapshot = new LinkedHashMap<>(size());
        for (Entry<K, V> e = head; e != null; e = e.next)
            snapshot.put(e.key, e.value);
        return snapshot;
    }

    @Override
    public String toString()
    {
        Entry<K, V> e = head;
        if (e == null)
            return "{}"; //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;)
        {
            sb.append(e);
            e = e.next;
            if (e == null)
                return sb.append('}').toString();
            sb.append(", "); //$NON-NLS-1$
        }
    }

    /**
     * Returns the most recently used cache entry, or
     * <code>null</code> if this cache is empty.
     *
     * @return the MRU entry, or <code>null</code> if the cache is empty
     */
    protected final Entry<K, V> getMruEntry()
    {
        return head;
    }

    /**
     * Returns the least recently used cache entry, or
     * <code>null</code> if this cache is empty.
     *
     * @return the LRU entry, or <code>null</code> if the cache is empty
     */
    protected final Entry<K, V> getLruEntry()
    {
        return tail;
    }

    /**
     * Returns the corresponding entry for the given key, or
     * <code>null</code> if this cache contains no entry for the key.
     *
     * @param key the key whose corresponding entry is to be returned
     * @return the corresponding entry for the given key, or
     *  <code>null</code> if the cache contains no entry for the key
     */
    protected final Entry<K, V> entryByKey(Object key)
    {
        return map.get(key);
    }

    /**
     * Creates a new cache entry with the given key and value.
     *
     * @param key the key of the new entry (never <code>null</code>)
     * @param value the value of the new entry (never <code>null</code>)
     * @return the created entry (not <code>null</code>)
     */
    protected Entry<K, V> newEntry(K key, V value)
    {
        return new Entry<>(key, value);
    }

    /**
     * Adds a new entry to this cache in response to
     * {@link #put(Object, Object)}.
     * <p>
     * This implementation invokes {@link #doAdd(Entry)}.
     * </p>
     *
     * @param entry the entry to add (never <code>null</code>)
     */
    protected void add(Entry<K, V> entry)
    {
        doAdd(entry);
    }

    /**
     * Updates an existing cache entry to change its value
     * and moves it to the MRU position in response to
     * {@link #put(Object, Object)}.
     * <p>
     * This implementation changes the entry value and then invokes
     * {@link #moveToMru(Entry)}.
     * </p>
     *
     * @param entry the entry to update (never <code>null</code>)
     * @param value a new value for the entry (never <code>null</code>)
     */
    protected void update(Entry<K, V> entry, V value)
    {
        entry.value = value;
        moveToMru(entry);
    }

    /**
     * Removes an existing entry from this cache in response to
     * {@link #remove(Object)}.
     * <p>
     * This implementation invokes {@link #doRemove(Entry)}.
     * </p>
     *
     * @param entry the entry to remove (never <code>null</code>)
     */
    protected void remove(Entry<K, V> entry)
    {
        doRemove(entry);
    }

    /**
     * Actually adds a new entry to this cache.
     *
     * @param entry the entry to add (never <code>null</code>)
     */
    protected void doAdd(Entry<K, V> entry)
    {
        map.put(entry.key, entry);
        linkHead(entry);
    }

    /**
     * Actually removes an existing entry from this cache.
     *
     * @param entry the entry to remove (never <code>null</code>)
     */
    protected void doRemove(Entry<K, V> entry)
    {
        map.remove(entry.key);
        unlink(entry);
    }

    /**
     * Moves an existing cache entry to the MRU position.
     *
     * @param entry the entry to move (never <code>null</code>)
     */
    protected void moveToMru(Entry<K, V> entry)
    {
        unlink(entry);
        linkHead(entry);
    }

    private void linkHead(Entry<K, V> entry)
    {
        entry.prev = null;
        entry.next = head;
        if (head == null)
            tail = entry;
        else
            head.prev = entry;
        head = entry;
    }

    private void unlink(Entry<K, V> entry)
    {
        Entry<K, V> prev = entry.prev;
        Entry<K, V> next = entry.next;
        if (prev != null)
            prev.next = next;
        if (next != null)
            next.prev = prev;
        if (head == entry)
            head = next;
        if (tail == entry)
            tail = prev;
    }

    /**
     * An LRU cache entry. Entries are ordered in the cache from
     * most recently accessed to least recently accessed.
     */
    protected static class Entry<K, V>
    {
        /**
         * The key of this entry (never <code>null</code>).
         */
        public final K key;

        /**
         * The value of this entry (never <code>null</code>).
         */
        public V value;

        Entry<K, V> prev, next;

        /**
         * Constructs a cache entry with the given key and value.
         *
         * @param key the key of the entry (never <code>null</code>)
         * @param value the value of the entry (never <code>null</code>)
         */
        public Entry(K key, V value)
        {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns the cache entry that is immediately followed by this entry,
         * or <code>null</code> if this is the MRU entry.
         *
         * @return the cache entry that is immediately followed by this entry,
         *  or <code>null</code> if this is the MRU entry
         */
        public final Entry<K, V> prev()
        {
            return prev;
        }

        /**
         * Returns the cache entry that immediately follows this entry,
         * or <code>null</code> if this is the LRU entry.
         *
         * @return the cache entry that immediately follows this entry,
         *  or <code>null</code> if this is the LRU entry
         */
        public final Entry<K, V> next()
        {
            return next;
        }

        @Override
        public String toString()
        {
            return key.toString() + '=' + value;
        }
    }
}
