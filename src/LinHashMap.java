package src;

/************************************************************************************
 * @file LinHashMap.java
 *
 * @author Arjun V. Sivanesan
 */

import java.io.*;
import java.lang.reflect.Array;
import static java.lang.System.out;
import java.util.*;

/************************************************************************************
 * This class provides hash maps that use the Linear Hashing algorithm.
 * A hash table is created that is an expandable array-list of buckets.
 */
public class LinHashMap<K, V>
        extends AbstractMap<K, V>
        implements Serializable, Cloneable {

    /**
     * The debug flag
     */
    private static final boolean DEBUG = true;

    /**
     * The number of slots (for key-value pairs) per bucket.
     */
    private static final int SLOTS = 4;

    /**
     * The threshold/upper bound on the ok so i factor
     */
    private static final double THRESHOLD = 1.2;

    /**
     * The class for type K.
     */
    private final Class<K> classK;

    /**
     * The class for type V.
     */
    private final Class<V> classV;

    /********************************************************************************
     * This inner class defines buckets that are stored in the hash table.
     */
    private class Bucket {

        // number of keys in the bucket
        int nKeys;

        // array to store keys
        K[] key;

        // array to store values corresponding to keys
        V[] value;

        // reference to the next bucket in case of collision
        Bucket next;

        @SuppressWarnings("unchecked")

        Bucket() {

            // initialize keys to 0
            nKeys = 0;

            // make array to hold keys with the slots
            key = (K[]) Array.newInstance(classK, SLOTS);

            // make array to hold values with the slots
            value = (V[]) Array.newInstance(classV, SLOTS);

            // initialize next bucket to null
            next = null;

        } // constructor

        // used to search for a value corresponding to a given key('K')
        V find(K k) {

            // loop through the keys in the bucket
            for (var j = 0; j < nKeys; j++) {

                // check if the current key matches the target key
                if (key[j].equals(k)) {

                    // return the corresponding value if a match is found
                    return value[j];

                } // if

            } // for

            // return null if the key is not found in the bucket
            return null;

        } // find

        // add a key-value pair to the bucket
        void add(K k, V v) {

            // add the key at the current number of keys
            key[nKeys] = k;

            // add the value at the current number of keys
            value[nKeys] = v;

            // increment the number of keys in the bucket
            nKeys++;

        } // add

        // print the keys in the bucket
        void print() {

            // print the opening bucket
            out.print("[ ");

            // loop through the keys in the bucket
            for (var j = 0; j < nKeys; j++) {
                // print each key followed by a delimiter
                out.print(key[j] + " | ");
            } // for

            // print the closing bracket and move to the next line
            out.println("]");

        } // print

    } // Bucket inner class

    /**
     * The list of buckets making up the hash table.
     */
    private final List<Bucket> hTable;

    /**
     * The modulus for low resolution hashing
     */
    private int mod1;

    /**
     * The modulus for high resolution hashing
     */
    private int mod2;

    /**
     * The index of the next bucket to split.
     */
    private int isplit = 0;

    /**
     * Counter for the number buckets accessed (for performance testing).
     */
    private int count = 0;

    /**
     * The counter for the total number of keys in the LinHash Map
     */
    private int keyCount = 0;

    /********************************************************************************
     * Construct a hash table that uses Linear Hashing.
     * 
     * @param classK the class for keys (K)
     * @param classV the class for values (V)
     */
    public LinHashMap(Class<K> _classK, Class<V> _classV) {

        // assign the key class
        classK = _classK;

        // ass the value class
        classV = _classV;

        // initial size of hash table (mod1)
        mod1 = 4;

        // secondary size of the hash table (mod2)
        mod2 = 2 * mod1;

        // initialize the hash table as an ArrayList
        hTable = new ArrayList<>();

        // initialize each bucket in the has table
        for (var i = 0; i < mod1; i++) {
            // add a new Bucket object to the has table
            hTable.add(new Bucket());
        } // for

    } // constructor

    /********************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * 
     * @return the set view of the map
     */
    public Set<Map.Entry<K, V>> entrySet() {

        // create a new HashSet to store the entries
        var enSet = new HashSet<Map.Entry<K, V>>();

        // iterate over the buckets in the hash table
        for (var bucket : hTable) {

            // iterate over the keys in each bucket
            for (int j = 0; j < bucket.nKeys; j++) {

                // create a new Map.Entry for each key-value pair and add it to the set
                Map.Entry<K, V> entry = new AbstractMap.SimpleEntry<>(bucket.key[j], bucket.value[j]);

                // add the entry (key-value pair) to the set of entries.
                enSet.add(entry);

            } // for

        } // for

        // return the set of entries
        return enSet;

    } // entrySet

    /********************************************************************************
     * Given the key, look up the value in the hash table.
     * 
     * @param key the key used for look up
     * @return the value associated with the key
     */
    @SuppressWarnings("unchecked")

    // retrieve the value associated with the specified key
    public V get(Object key) {

        // calculate the hash value for the key
        var i = h(key);

        // find the key in the hash table and return its associated value
        return find((K) key, hTable.get(i), true);

    } // get

    /********************************************************************************
     * Put the key-value pair in the hash table. Split the 'isplit' bucket chain
     * when the load factor is exceeded.
     * 
     * @param key   the key to insert
     * @param value the value to insert
     * @return the old/previous value, null if none
     */
    public V put(K key, V value) {

        // increment total key count
        keyCount++;

        // calculate the current load factor
        var loadFactor = loadFactor();

        // if the load factor exceeds the threshold, split the bucket chain
        if (loadFactor > THRESHOLD) {
            split();
        } // if

        // find the index of the bucket chain to insert the key-value pair
        var index = findRightBucket(key);

        // retrieve the home bucket where the key-value pair should be inserted
        var homeBucket = hTable.get(index);

        // find the old value associated with the key, if any
        var oldValue = find(key, homeBucket, false);

        // print debug information about the put operation
        out.println("LinearHashMap.put: key = " + key + ", h() = " + index + ", value = " + value);

        // start with the home bucket as the current bucket
        var currentBucket = homeBucket;

        // loop until a bucket with available slots is found
        while (true) {

            // if the current bucket has available slots
            if (currentBucket.nKeys < SLOTS) {

                // add the key-value pair
                currentBucket.add(key, value);

                // return the old value
                return oldValue;

            } // if

            // if the current bucket is full and has a next bucket in the chain
            if (currentBucket.next != null) {
                // move to the next bucket
                currentBucket = currentBucket.next;
            } // if

            else {
                // if the current bucket is the last in the chain, break
                break;
            } // else

        } // while

        // if no available slots were found
        // create a new bucket
        var newBucket = new Bucket();

        // add it to the end of the chain
        newBucket.add(key, value);
        currentBucket.next = newBucket;

        // return the old value associated wit the key, if any
        return oldValue;

    } // put

    /********************************************************************************
     * Print the hash table.
     */
    public void print() {

        // print header indicating the type of hash map
        out.println("LinHashMap");
        out.println("-------------------------------------------");

        // iterate over each bucket in the hash table
        for (var i = 0; i < hTable.size(); i++) {

            // print the index of the current bucket
            out.print("Bucket [ " + i + " ] = ");
            var j = 0;

            // iterate over each bucket chain starting from the current bucket
            for (var b = hTable.get(i); b != null; b = b.next) {

                // if this is not the first bucket in the chain, print a separator
                if (j > 0) {
                    out.print(" \t\t --> ");
                } // if

                // print the contents of the current bucket
                b.print();
                j++;

            } // for

        } // for

        // print the footer separator
        out.println("-------------------------------------------");

    } // print

    /********************************************************************************
     * Return the size (SLOTS * number of home buckets) of the hash table.
     * 
     * @return the size of the hash table
     */
    public int size() {
        return SLOTS * (mod1 + isplit);
    } // size

    /********************************************************************************
     * Split bucket chain 'isplit' by creating a new bucket chain at the end of the
     * hash table and redistributing the keys according to the high resolution hash
     * function 'h2'. Increment 'isplit'. If current split phase is complete,
     * reset 'isplit' to zero, and update the hash functions.
     */
    public void split() {

        // create a new bucket chain at the end of the hash table
        hTable.add(new Bucket());

        // redistribute the keys according to the high resolution hash function h2
        for (int i = 0; i < hTable.get(isplit).nKeys; i++) {

            K k = hTable.get(isplit).key[i];
            V v = hTable.get(isplit).value[i];

            // calculate new index using h2
            int index = h2(k) % hTable.size();

            // add key-value pair to new bucket chain
            hTable.get(index).add(k, v);

        } // for

        // increment isplit
        isplit++;

        // check if current split phase is complete
        if (isplit >= mod1) {
            // reset isplit to zero
            isplit = 0;
            // update mod1
            mod1 *= 2;
            // update mod2
            mod2 = 2 * mod1;
        } // if

        out.println("split: bucket chain " + isplit);

        /*
         * int numBucketsToAdd = 1;
         * 
         * var bucket = hTable.get(isplit);
         * while(bucket.next != null) {
         * numBucketsToAdd++;
         * bucket = bucket.next;
         * hTable.add(new Bucket());
         * } //while
         * 
         * hTable.add(new Bucket());
         * 
         * bucket = hTable.get(isplit);
         * 
         * while(bucket != null) {
         * out.print("numKeys: " + bucket.nKeys);
         * for(int i = 0; i < bucket.nKeys; i++) {
         * if(i > SLOTS) {
         * out.println("i is, for some reason, this: " + i);
         * } //if
         * 
         * var bucketIndex = bucket.key[i].hashCode() % (mod1 + numBucketsToAdd);
         * hTable.get(bucketIndex).add(bucket.key[i], bucket.value[i]);
         * } //for
         * bucket = bucket.next;
         * } //while
         * 
         * hTable.set(isplit, new Bucket());
         * 
         * isplit++;
         * var lf = loadFactor();
         * if(lf <= THRESHOLD) {
         * isplit = 0;
         * }
         * 
         * mod1 = hTable.size();
         * 
         * 
         * //debug statement
         * out.println("split: bucket chain " + isplit);
         */

    } // split()

    /********************************************************************************
     * Return the load factor for the hash table.
     * 
     * @return the load factor
     */
    private double loadFactor() {
        return keyCount / (double) size();
    } // loadFactor

    /********************************************************************************
     * Find the key in the bucket chain that starts with home bucket bh
     * 
     * @param key    the key to find
     * @param bh     the given home bucket
     * @param by_get indicates whether 'find' is called from 'get' (performance
     *               monitored)
     * @return the current value stored stored for the key
     */
    private V find(K key, Bucket bh, boolean by_get) {

        // iterate over the bucket chain starting from 'bh'
        for (var b = bh; b != null; b = b.next) {

            // increment over the bucket if 'by_get' is true (for performance monitoring)
            if (by_get) {
                count++;
            } // if

            // find the key in the current bucket 'b'
            V result = b.find(key);

            // return the result if the key is found in the current bucket
            if (result != null) {
                return result;
            } // if

        } // for

        // return null if the key is not found in any bucket in the chain
        return null;

    } // find

    /************************************************************************************
     * Find the correct bucket index for a given key after the split operation.
     * 
     * @param key the key to find the bucket index for
     * @return the index of the bucket for the given key
     */
    private int findRightBucket(Object key) {

        // calculate hash value using high resolution hash function
        int ret = h2(key);

        // if index is out of bounds, use low resolution hash function
        if (ret >= hTable.size()) {
            return h(key);
        } // if

        // return the calculated index
        return ret;

    } // findRightBucket

    /********************************************************************************
     * Hash the key using the low resolution hash function.
     * 
     * @param key the key to hash
     * @return the location of the bucket chain containing the key-value pair
     */
    private int h(Object key) {

        // calculate the hash value using the hashCode of the key and the current
        // modulus 'mod1'
        int ret = key.hashCode() % mod1;

        // adjust the hash value to ensure it's non-negative
        if (ret < 0) {
            ret += mod1;
        } // if

        // return the adjusted hash value
        return ret;

    } // h

    /********************************************************************************
     * Hash the key using the high resolution hash function.
     * 
     * @param key the key to hash
     * @return the location of the bucket chain containing the key-value pair
     */
    private int h2(Object key) {

        // calculate the hash value using the hashCode of the key and the modulus 'mod2'
        int ret = key.hashCode() % mod2;

        // adjust the hash value to ensure it's non-negative
        if (ret < 0) {
            ret += mod2;
        } // if

        // return the adjusted hash value
        return ret;

    } // h2

    /********************************************************************************
     * The main method used for testing.
     * 
     * @param the command-line arguments (args [0] gives number of keys to insert)
     */
    public static void main(String[] args) {

        // default number of keys to insert
        var totalKeys = 40;

        // flag to insert keys randomly or sequentially
        var RANDOMLY = false;

        // create a new instance of LinHasMap with Integer keys and Integer values
        LinHashMap<Integer, Integer> ht = new LinHashMap<>(Integer.class, Integer.class);

        // check if a command-line argument is provided for the total number of keys
        if (args.length == 1) {
            totalKeys = Integer.valueOf(args[0]);
        } // if

        // insert keys and values into the hash table
        if (RANDOMLY) {
            // insert keys randomly using a random number generator
            var rng = new Random();
            for (var i = 1; i <= totalKeys; i += 2) {
                ht.put(rng.nextInt(2 * totalKeys), i * i);
            } // for
        } // if

        else {
            // insert keys sequentially
            for (var i = 1; i <= totalKeys; i += 2) {
                ht.put(i, i * i);
            } // for
        } // else

        // print the hash table
        ht.print();

        // retrieve and print the values for keys from 0 to totalKeys
        for (var i = 0; i <= totalKeys; i++) {
            out.println("key = " + i + " value = " + ht.get(i));
        } // for

        // print the average number of buckets accessed during the operations
        out.println("-------------------------------------------");
        out.println("Average number of buckets accessed = " + ht.count / (double) totalKeys);
    } // main

} // LinHashMap class
