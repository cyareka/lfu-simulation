package com.group4.lfu_simulation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LFUCache {
    private Map<Integer, Integer> valueMap = new HashMap<>();
    private Map<Integer, Integer> countMap = new HashMap<>();
    private final TreeMap<Integer, List<Integer>> frequencyMap = new TreeMap<>();
    private final int size;
    
    public LFUCache(int capacity) {
        size = capacity;
    }
    
    /**
     * This function retrieves the value associated with a given key from a map, updates the frequency
     * of the key in a count map, and moves the key to a new frequency list in a frequency map.
     * 
     * @param key The key of the element that we want to retrieve from the data structure.
     * @return The method is returning an integer value, which is the value associated with the given
     * key in the valueMap. If the key is not present in the valueMap or the size of the map is 0, then
     * it returns -1.
     */
    public int get(int key) {
        if (valueMap.containsKey(key) == false || size == 0)
            return -1;
        int freq = countMap.get(key);
        frequencyMap.get(freq).remove(Integer.valueOf(key));

        if (frequencyMap.get(freq).size() == 0)
            frequencyMap.remove(freq);
        
        frequencyMap.computeIfAbsent(freq + 1, k -> new LinkedList<>()).add(key);
        countMap.put(key, freq + 1);
        return valueMap.get(key);
    }

    public void put(int key, int value) {
        if (valueMap.containsKey(key) == false && size > 0) {
            if (valueMap.size() == size) {
                int lowestCount = frequencyMap.firstKey();
                int keyToDelete = frequencyMap.get(lowestCount).remove(0);

                if (frequencyMap.get(lowestCount).size() == 0) 
                    frequencyMap.remove(lowestCount);
                
                valueMap.remove(keyToDelete);
                countMap.remove(keyToDelete);
            }
            valueMap.put(key, value);
            countMap.put(key, 1);
            frequencyMap.computeIfAbsent(1, k -> {
                LinkedList<Integer> list = new LinkedList<>();
                list.add(key);
                return list;
            });
        } else if (size > 0) {
            valueMap.put(key, value);
            int freq = countMap.get(key);
            frequencyMap.get(freq).remove(Integer.valueOf(key));

            if (frequencyMap.get(freq).size() == 0)
                frequencyMap.remove(freq);

            frequencyMap.computeIfAbsent(1, k -> {
                LinkedList<Integer> list = new LinkedList<>();
                list.add(key);
                return list;
            });
            countMap.put(key, freq + 1);
        }
    }

    public class Main {
        public static void main(String[] args) {
            // Create an instance of LFUCache with a capacity of 3
            LFUCache cache = new LFUCache(3);
            
            // Insert key-value pairs into the cache
            cache.put(1, 10);
            cache.put(2, 20);
            cache.put(3, 30);
            
            // Retrieve values from the cache
            System.out.println(cache.get(1));  // Output: 10
            System.out.println(cache.get(2));  // Output: 20
            
            // Insert another key-value pair, which exceeds the cache capacity
            cache.put(4, 40);
            
            // The least frequently used key (3) should have been evicted
            System.out.println(cache.get(3));  // Output: -1
            
            // Retrieve the values for the remaining keys
            System.out.println(cache.get(1));  // Output: 10
            System.out.println(cache.get(2));  // Output: 20
            System.out.println(cache.get(4));  // Output: 40
        }
    }
}
