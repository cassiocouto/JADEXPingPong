package jadex.commons.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *  A bidirectional hash map. Note using this implies a bijection (1:1 relation).
 *
 */
public class BiHashMap<K, V> implements Map<K, V>
{
	/** The forward map. */
	protected HashMap<K, V> forward;

	/** The reverse map. */
	protected HashMap<V, K> reverse;
	
	public BiHashMap()
	{
		forward = new HashMap<K, V>();
		reverse = new HashMap<V, K>();
	}
	
	/**
	 *  Clears the map.
	 */
	public void clear()
	{
		forward.clear();
		reverse.clear();
	}
	
	/**
     * Returns whether the forward map contains the specified key.
     *
     * @param key The key.
     * @return True, if the forward map contains key.
     */
	public boolean containsKey(Object key)
	{
		return forward.containsKey(key);
	}

	/**
     * Returns whether the reverse map contains the specified key.
     *
     * @param key The key.
     * @return True, if the reverse map contains key.
     */
	public boolean containsValue(Object value)
	{
		return reverse.containsKey(value);
	}

	/**
     * Returns the entry set of the forward map.
     *
     * @return The entry set of the forward map.
     */
	public Set<Map.Entry<K, V>> entrySet()
	{
		return forward.entrySet();
	}
	
	/**
     * Returns the entry set of the reverse map.
     *
     * @return The entry set of the reverse map.
     */
	public Set<Map.Entry<V, K>> rentrySet()
	{
		return reverse.entrySet();
	}

	/**
     *  Returns the value of the key from the forward map.
     * 
     *  @return The value.
     */
	public V get(Object key)
	{
		return forward.get(key);
	}
	
	/**
     *  Returns the value of the key from the reverse map.
     * 
     *  @return The value.
     */
	public K rget(Object key)
	{
		return reverse.get(key);
	}
	
	/**
	 *  Tests if the map is empty.
	 *  
	 *  @return True, if empty.
	 */
	public boolean isEmpty()
	{
		return forward.isEmpty();
	}

	/**
	 *  Returns the keys of the forward map.
	 *  
	 *  @return The keys.
	 */
	public Set<K> keySet()
	{
		return forward.keySet();
	}

	/**
	 *  Puts an entry into the map, forward direction.
	 *  
	 *  @param key The key.
	 *  @param value The value.
	 *  @return The value.
	 */
	public V put(K key, V value)
	{
		reverse.put(value, key);
		return forward.put(key, value);
	}

	/**
	 *  Returns the size of the map.
	 *  
	 *  @return The size of the map.
	 */
	public int size()
	{
		return forward.size();
	}

	/**
	 *  Removes an entry, forward direction.
	 *  
	 *  @param key The entry key.
	 *  @return The removed value.
	 */
	public V remove(Object key)
	{
		V ret = forward.remove(key);
		reverse.remove(ret);
		return ret;
	}
	
	/**
	 *  Removes an entry, reverse direction.
	 *  
	 *  @param key The entry key.
	 *  @return The removed value.
	 */
	public K rremove(Object key)
	{
		K ret = reverse.remove(key);
		forward.remove(ret);
		return ret;
	}

	/**
	 *  Puts all entries of a map into this map, forward direction.
	 *  
	 *  @param m The map.
	 */
	public void putAll(Map<? extends K, ? extends V> m)
	{
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet())
		{
			put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 *  Puts all entries of a map into this map, reverse direction.
	 *  
	 *  @param m The map.
	 */
	public void rputAll(Map<? extends V, ? extends K> m)
	{
		for (Map.Entry<? extends V, ? extends K> entry : m.entrySet())
		{
			put(entry.getValue(), entry.getKey());
		}
	}

	/**
	 *  Returns the keys of the reverse map.
	 *  
	 *  @return The keys.
	 */
	public Collection<V> values()
	{
		return reverse.keySet();
	}
	
}
