package com.group4.lfu_simulation;

import java.util.*;

public class LFUSimulation {
    private static final int NUM_ROWS = 3;
    private static final int MAX_NUMBERS = 15;

    public static void main(String[] args) {
        LFUCache lfuCache = new LFUCache(NUM_ROWS);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter up to 15 numbers (separated by spaces): ");
        String input = scanner.nextLine();
        scanner.close();

        String[] numbers = input.split(" ");
        List<Integer> numberList = new ArrayList<>();

        for (String number : numbers) {
            try {
                int num = Integer.parseInt(number);
                numberList.add(num);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number: " + number);
            }
        }

        System.out.println("\nSimulation Results:");
        System.out.println("Number\t| Operation\t| Cache State\t| Frequencies");

        for (int number : numberList) {
            simulateLFU(lfuCache, number);
        }

        System.out.println("\nFinal Frequency Table:");
        lfuCache.displayTable();
    }

    /**
     * The function simulates the LFU cache replacement algorithm by checking if a number is in the
     * cache, incrementing its frequency if it is, removing the least frequently used number if the
     * cache is full, and inserting the new number into the cache.
     * 
     * @param lfuCache an instance of the LFUCache class, which represents the cache being simulated.
     * @param number The number being requested or inserted into the cache.
     */
    private static void simulateLFU(LFUCache lfuCache, int number) {
        String operation = "";
        String cacheState = "";
        String frequencies = "";

        // Check if the number is already in the cache
        // This code block is checking if the LFU cache contains the given number. If it does, it
        // increments the frequency of the number in the cache, sets the operation to "Page Hit", and
        // updates the cache state and frequencies. If it does not, it sets the operation to "Page
        // Fault", updates the cache state and frequencies, and inserts the new number into the cache.
        if (lfuCache.contains(number)) {
            operation = "Page Hit";
            cacheState = lfuCache.toString();
            frequencies = lfuCache.getFrequencies().toString();
            lfuCache.incrementFrequency(number);
        } else {
            operation = "Page Fault";
            cacheState = lfuCache.toString();
            frequencies = lfuCache.getFrequencies().toString();

            // If the cache is full, remove the least frequently used number
            if (lfuCache.isFull()) {
                int removedNumber = lfuCache.removeLFU();
                cacheState = lfuCache.toString();
                frequencies = lfuCache.getFrequencies().toString();
                operation += " (Removed: " + removedNumber + ")";
            }

            // Insert the new number into the cache
            int addedRow = lfuCache.insert(number);
            cacheState = lfuCache.toString();
            frequencies = lfuCache.getFrequencies().toString();
            operation += " (Added to Row: " + addedRow + ")";
        }

        // Display the simulation results
        System.out.println(number + "\t| " + operation + "\t| " + cacheState + "\t| " + frequencies);
    }
}

class LFUCache {
    private int numRows;
    private Map<Integer, Integer> keyToFrequency;
    private TreeMap<Integer, LinkedHashSet<Integer>> frequencyToKeys;
    private Map<Integer, Integer> keyToRow;

    // `public LFUCache(int numRows)` is a constructor for the `LFUCache` class that takes an integer
    // `numRows` as a parameter. It initializes the `numRows` field of the `LFUCache` object to the
    // value of `numRows`, and initializes three data structures: a `HashMap` called `keyToFrequency`
    // that maps keys to their frequencies, a `TreeMap` called `frequencyToKeys` that maps frequencies
    // to sets of keys with that frequency, and a `HashMap` called `keyToRow` that maps keys to the row
    // in which they are stored in the cache.
    public LFUCache(int numRows) {
        this.numRows = numRows;
        keyToFrequency = new HashMap<>();
        frequencyToKeys = new TreeMap<>();
        keyToRow = new HashMap<>();
    }

    // The `public boolean contains(int key)` method is checking if the LFU cache contains a given key.
    // It does this by checking if the `keyToFrequency` map contains the given key. If it does, it
    // returns `true`, indicating that the key is in the cache. If it does not, it returns `false`,
    // indicating that the key is not in the cache.
    public boolean contains(int key) {
        return keyToFrequency.containsKey(key);
    }

    // The `public void incrementFrequency(int key)` method is used to increment the frequency of a key
    // in the LFU cache. It takes an integer `key` as a parameter and first retrieves the current
    // frequency of the key from the `keyToFrequency` map. It then increments the frequency by 1 and
    // updates the `keyToFrequency` map with the new frequency.
    public void incrementFrequency(int key) {
        int frequency = keyToFrequency.get(key);
        keyToFrequency.put(key, frequency + 1);

        LinkedHashSet<Integer> keysWithFrequency = frequencyToKeys.get(frequency);
        keysWithFrequency.remove(key);

        if (keysWithFrequency.isEmpty()) {
            frequencyToKeys.remove(frequency);
        }

        frequencyToKeys.computeIfAbsent(frequency + 1, k -> new LinkedHashSet<>()).add(key);
    }

    // The `public int insert(int key)` method is used to insert a new key into the LFU cache. It takes
    // an integer `key` as a parameter and first checks if the cache is full. If the cache is not full,
    // it assigns the new key to the next available row in the cache. If the cache is full, it removes
    // the least frequently used key from the cache and assigns the new key to the row that the removed
    // key was in. It then sets the frequency of the new key to 1, adds the new key to the
    // `frequencyToKeys` map with a frequency of 1, and adds the new key to the `keyToRow` map with the
    // row it was assigned to. Finally, it returns the row that the new key was assigned to.
    public int insert(int key) {
        int row = 1;
        if (keyToRow.size() < numRows) {
            row = keyToRow.size() + 1;
        } else {
            int minFrequency = frequencyToKeys.firstKey();
            LinkedHashSet<Integer> keysWithMinFrequency = frequencyToKeys.get(minFrequency);
            int removedKey = keysWithMinFrequency.iterator().next();
            keysWithMinFrequency.remove(removedKey);
            keyToFrequency.remove(removedKey);
            row = keyToRow.get(removedKey);
            keyToRow.remove(removedKey);
        }

        keyToFrequency.put(key, 1);
        frequencyToKeys.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
        keyToRow.put(key, row);

        return row;
    }

    // The `public int removeLFU() {` method is used to remove the least frequently used key from the
    // LFU cache. It first retrieves the lowest frequency from the `frequencyToKeys` map, and then
    // retrieves the set of keys with that frequency. It removes the first key in the set, which is the
    // least recently used key with the lowest frequency, and removes it from the `keyToFrequency` and
    // `keyToRow` maps. If the set of keys with the lowest frequency is empty after removing the key,
    // it removes the frequency from the `frequencyToKeys` map. Finally, it returns the key that was
    // removed, or -1 if no key was removed.
    public int removeLFU() {
        int removedKey = -1;
        int lowestFrequency = frequencyToKeys.firstKey();
        LinkedHashSet<Integer> keysWithLowestFrequency = frequencyToKeys.get(lowestFrequency);

        if (!keysWithLowestFrequency.isEmpty()) {
            removedKey = keysWithLowestFrequency.iterator().next();
            keysWithLowestFrequency.remove(removedKey);

            if (keysWithLowestFrequency.isEmpty()) {
                frequencyToKeys.remove(lowestFrequency);
            }

            keyToFrequency.remove(removedKey);
            keyToRow.remove(removedKey);
        }

        return removedKey;
    }

    // The `public boolean isFull() {` method is checking if the LFU cache is full. It does this by
    // checking if the size of the `keyToFrequency` map is equal to the number of rows in the cache
    // (`numRows`). If the size of the map is equal to `numRows`, it returns `true`, indicating that
    // the cache is full. If the size of the map is less than `numRows`, it returns `false`, indicating
    // that the cache is not full.
    public boolean isFull() {
        return keyToFrequency.size() == numRows;
    }

    // The `public Map<Integer, Integer> getFrequencies() {` method is returning a copy of the
    // `keyToFrequency` map, which maps keys to their frequencies in the LFU cache. The method creates
    // a new `HashMap` object and adds all the entries from the `keyToFrequency` map to the new map.
    // This is done to prevent external modification of the `keyToFrequency` map, which could cause
    // unexpected behavior in the LFU cache. The returned map can be used to display the frequencies of
    // the keys in the cache.
    public Map<Integer, Integer> getFrequencies() {
        return new HashMap<>(keyToFrequency);
    }

   // The `public void displayTable() {` method is used to display the frequency table of the LFU
   // cache. It prints out the frequencies of the keys in the cache, along with the keys themselves, in
   // descending order of frequency. The method first prints out a header for the table, which consists
   // of the strings "Frequency" and "Numbers" separated by a tab character. It then iterates over the
   // frequencies in the `frequencyToKeys` map in descending order using the `descendingKeySet()`
   // method. For each frequency, it retrieves the set of keys with that frequency from the
   // `frequencyToKeys` map and prints out the frequency and the set of keys separated by a tab
   // character. The method uses the `System.out.println()` method to print out each row of the table.
    public void displayTable() {
        System.out.println("Frequency\t| Numbers");
        for (int frequency : frequencyToKeys.descendingKeySet()) {
            LinkedHashSet<Integer> keysWithFrequency = frequencyToKeys.get(frequency);
            System.out.println(frequency + "\t\t| " + keysWithFrequency);
        }
    }

    @Override
    // The `public String toString() {` method is overriding the default `toString()` method of the
    // `Object` class for the `LFUCache` class. It returns a string representation of the cache state,
    // which consists of the frequencies of the keys in the cache, along with the keys themselves, in
    // descending order of frequency. The method first creates a `StringBuilder` object and appends
    // each frequency and its corresponding set of keys to the string. It then returns the string
    // representation of the cache state. This method is used in the `simulateLFU()` method to display
    // the cache state in the simulation results.
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int frequency : frequencyToKeys.descendingKeySet()) {
            LinkedHashSet<Integer> keysWithFrequency = frequencyToKeys.get(frequency);
            sb.append(frequency).append("\t\t| ").append(keysWithFrequency).append("\n");
        }
        return sb.toString();
    }
}
