package org.mhj.cachesim;

/**
 * Created by edward on 16-4-7.
 */
public abstract class Storage {

    public int getLoad_count() {
        return load_count;
    }

    public void setLoad_count(int load_count) {
        this.load_count = load_count;
    }

    public int getStore_count() {
        return store_count;
    }

    public void setStore_count(int store_count) {
        this.store_count = store_count;
    }

    protected int load_count;
    protected int store_count;

    public int getStore_miss_count() {
        return store_miss_count;
    }

    public void setStore_miss_count(int store_miss_count) {
        this.store_miss_count = store_miss_count;
    }

    public int getLoad_miss_count() {
        return load_miss_count;
    }

    public void setLoad_miss_count(int load_miss_count) {
        this.load_miss_count = load_miss_count;
    }

    protected int load_miss_count;
    protected int store_miss_count;



    protected Storage() {
        load_count = 0;
        store_count = 0;
    }

    public abstract int load(int address);
    public abstract int store(int address);
}
