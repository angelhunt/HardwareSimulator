package org.mhj.cachesim;

/**
 * Created by edward on 16-4-7.
 */
public class Memory extends Storage {
    @Override
    public int load(int address) {
        int cycle = 100;
        load_count++;
        return cycle;
    }

    @Override
    public int store(int address) {
        int cycle = 100;
        store_count++;
        return cycle;
    }
}
