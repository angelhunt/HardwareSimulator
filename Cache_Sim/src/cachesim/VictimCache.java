package cachesim;

/**
 * Created by edward on 16-4-11.
 */
public class VictimCache extends Cache {

    VictimCache(CacheConfigure configure, Storage storage) {
        super(configure, storage);
    }



    @Override
    public Res load(int address) {
        if (debug)
            System.out.printf("Cache %d :", configure.no);
        int index = address & index_mask;
        int tag = address & tag_mask;

        Res res = find(index, tag);
        String state = "load complete!\n";
        clock_cycle += latency[configure.no];
        if (res.groupno == -1) {
            load_miss_count++;
            state = "load miss\n";
        }
        if (debug)
            System.out.print(state);
        load_count++;

        return res;
    }




    @Override
    public Res store(int address){
        if (debug)
            System.out.printf("Cache %d :", configure.no);
        int index = address & index_mask;
        int tag = address & tag_mask;

        Res res = find(index, tag);
        String state = "store complete!\n";
        clock_cycle += latency[configure.no];
        if (res.groupno == -1) {
            store_miss_count++;
            state = "store miss\n";
        }
        if (debug)
            System.out.print(state);
        store_count++;
        return res;
    }


    public void update(int address, int groupno) {
        int index = address & index_mask;
        int tag = address & tag_mask;

        clock_cycle += latency[configure.no];
        if (groupno == -1)
            replacement(index, tag);
        else{
            if(cache_set[groupno].containsKey(index))
                assert(cache_set[groupno].get(index) != tag);
            cache_set[groupno].put(index, tag);
        }
    }
    public void delete(int address, int groupno){
        int index = address & index_mask;
        int tag = address & tag_mask;
        cache_set[groupno].remove(index);
    }
}




