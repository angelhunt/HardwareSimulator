package cachesim;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by edward on 16-4-7.
 */
class Res{
    int groupno = -1;
    int emptyno = -1;
}
class CacheConfigure{
    int cachesize, blocksize, ass, RS, no;

    CacheConfigure(int cachesize, int blocksize, int ass, int RS, int no) {
        this.cachesize = cachesize;
        this.blocksize = blocksize;
        this.ass = ass;
        this.RS = RS;
        this.no = no;
    }

    public String toString(){

        String RSstr = RS == 0 ? "Random" : "LRU";
        return String.format("cachesize:%-10s, blocksize:%-10s, ass:%-10d, RS:%-10s",
                getCSstr(cachesize), getCSstr(blocksize), ass, RSstr);

    }

    private String[] sizestr = {
            "byte",
            "KB",
            "MB",
            "GB"
    };
    private String getCSstr(int a){
        StringBuilder s = new StringBuilder();
        int level = 0;
        while(a > 1024) {
            level++;
            a /= 1024;
        }

        return s.append(a).append(sizestr[level]).toString();

    }
}
public class Cache extends Storage {
    CacheConfigure configure = null;
    boolean debug = false;
    private HashMap<Integer, LinkedList<Integer>> order_map = null;

    public void setVictimcache(VictimCache victimcache) {
        this.victimcache = victimcache;
    }

    public VictimCache getVictimcache() {
        return victimcache;
    }

    private VictimCache victimcache = null;

    private Storage storage = null;
    private int block_bits = 5;
    private int tag_bits = 17;
    protected HashMap<Integer, Integer>[] cache_set = null;
    protected int tag_mask = ~((1 << (32 - tag_bits)) - 1);
    protected int index_mask = ~(tag_mask + ((1 << block_bits) - 1));

    protected static int[] latency = {1, 10, 100};
    public Cache(CacheConfigure configure, Storage storage){
        this.configure = configure;
        this.storage = storage;

        block_bits = (int)(Math.log(configure.blocksize * 8)  / Math.log(2));
        int index_bits = (int) (Math.log(configure.cachesize / configure.blocksize / configure.ass) / Math.log(2));
        tag_bits = 32 - block_bits - index_bits;
        tag_mask = ~((1 << (32 - tag_bits)) - 1);
        index_mask = ~(tag_mask + ((1 << block_bits) - 1));


        configure.ass = configure.ass == -1 ? configure.cachesize / configure.blocksize : configure.ass;
        cache_set = new HashMap[configure.ass];
        for(int i = 0; i < configure.ass; i++)
            cache_set[i] = new HashMap<>();

        if(configure.RS == 1){
            order_map = new HashMap<>();
        }


    }



    //根据index,tag寻找相应的组号
    protected Res find(int index, int tag){
        Res res = new Res();
        for(int i = 0; i < configure.ass; i++) {
            if (cache_set[i].containsKey(index)){
                if (res.groupno == -1 && (tag ^ cache_set[i].get(index)) == 0)
                    res.groupno = i;
            }
            else {
                if(res.emptyno == -1)
                    res.emptyno = i;
            }
            if(res.emptyno != -1 && res.groupno != -1)
                break;
        }

        //为实现ＬＲＵ处理相应的hash表
        if(configure.RS == 1) {
            LinkedList<Integer> order_list = null;
            if (!order_map.containsKey(index)) {
                order_list = new LinkedList<>();
                for (int i = 0; i < configure.ass; i++)
                    order_list.addLast(i);
                order_map.put(index, order_list);
            } else
                order_list = order_map.get(index);
            if (res.groupno != -1) {
                order_list.remove(res.groupno);
                order_list.addLast(res.groupno);
            }
        }
        return res;
    }

    protected int randomReplace(int index, int tag) {
        double rand = Math.random();
        int no = (int)(Math.random() * (configure.ass - 1));
        int res = cache_set[no].get(index);
        cache_set[no].put(index, tag);
        return res;
    }


    protected int LRU(int index, int tag){
        LinkedList<Integer> order_list = order_map.get(index);
        int deleted_index = order_list.getFirst();

        //替换
        assert cache_set[deleted_index].containsKey(index);
        int res = cache_set[deleted_index].get(index);
        cache_set[deleted_index].put(index, tag);

        order_list.removeFirst();
        order_list.addLast(deleted_index);
        return res;
    }

    protected int replacement(int index, int tag){
        switch(this.configure.RS){
            case 0:
                return randomReplace(index, tag);
            case 1:
                return LRU(index, tag);
            default:
                break;
        }
        return index;
    }

    @Override
    public Res load(int address){
        if(debug)
            System.out.printf("Cache %d :", configure.no);
        int index = address & index_mask;
        int tag = address & tag_mask;

        Res res = find(index, tag);
        String state = "load complete!\n";
        clock_cycle += latency[configure.no];
        if(res.groupno == -1){
            if (victimcache != null) {//存在victim
                Res victim_res = victimcache.load(address);
                if(victim_res.groupno == -1) { //victim miss
                    load_miss_count++;
                    storage.load(address);
                }
                update(index, tag, res.emptyno, victim_res);
            }
            else {//不存在victim_cache
                load_miss_count++;
                storage.load(address);
                update(index, tag, res.emptyno, null);
            }


            state = "load miss\n";
        }
        if(debug)
            System.out.print(state);
        load_count++;
        return res;
    }

    //根据index,tag更新cache, 需要消耗时钟
    private void update(int index, int tag, int emptyno, Res victim_res){
        clock_cycle += latency[configure.no];
        if(emptyno == -1){
            int replaced_tag = replacement(index, tag);
            if(victimcache != null){
                assert (victim_res != null);
                if(victim_res.groupno == -1) //victim miss
                    victimcache.update(index + replaced_tag, victim_res.emptyno);
                else
                    victimcache.update(index + replaced_tag, victim_res.groupno);
            }
        }else {
            if (cache_set[emptyno].containsKey(index)) {
                System.out.println("error happen in cache" + configure.no);
                System.exit(-1);
            }
            cache_set[emptyno].put(index, tag);
            if(victimcache != null && victim_res.groupno != -1)
                victimcache.delete(index + tag, victim_res.groupno);
        }
    }

    @Override
    public Res store(int address) {
        if(debug)
        System.out.printf("Cache %d :", configure.no);
        int index = address & index_mask;
        int tag = address & tag_mask;
        Res res = find(index, tag);
        clock_cycle += latency[configure.no];


        String state = "store complete!\n";
        if(res.groupno == -1) {//cache　miss
            if (victimcache != null) {//存在victim
                Res victim_res = victimcache.store(address);
                if (victim_res.groupno == -1) {
                    store_miss_count++;
                    storage.store(address);
                }
                update(index, tag, res.emptyno, victim_res);
            }
            else {//不存在victim_cache
                store_miss_count++;
                storage.store(address);
                update(index, tag, res.emptyno, null);
            }
            state = "store miss!\n";
        }
        if(debug)
            System.out.print(state);
        store_count++;
        return res;
    }

}


