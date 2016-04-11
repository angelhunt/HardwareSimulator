package org.mhj.cachesim;

import java.util.HashMap;

/**
 * Created by edward on 16-4-7.
 */
public class Cache extends Storage {
    private int cache_size = 32 * 1024;          //默认32 * 1024 bit
    private int block_size = 32;           //默认32bit
    private int associativity = 1;        //默认全映射
    private int RP = 0;                   //默认随机替换
    private Storage storage = null;
    private int no = 0;
    private static int latency[] = {1, 10};



    private int block_bits = 5;
    private int cache_line_bits = 10;
    private int cache_line_num = 1024;
    private int cache_set_num = 1024;

    private int index_bits = 10;
    private int tag_bits = 17;

    private HashMap<Integer, Integer> cacheset[] = null;

    private int tag_mask = ~((1 << (32 - tag_bits)) - 1);
    private int index_mask = ~(tag_mask + ((1 << block_bits) - 1));


    public Cache(){
        int temp = (1 << 15) - 1;
        cacheset = new HashMap[associativity];
        for(int i = 0; i < associativity; i++)
            cacheset[i] = new HashMap<Integer, Integer>();
        storage = new Memory();
    }
    public Cache(int cs, int bs, int ass, int R, Storage sto, int no){
        cache_size = cs;
        block_size = bs;
        associativity = ass;
        RP = R;
        storage = sto;
        this.no = no;
    }

    private class Res{
        int groupno = -1;
        int emptyno = -1;
    }
    //根据index,tag寻找相应的组号
    public Res find(int index, int tag){
        Res res = new Res();
        for(int i = 0; i < associativity; i++)
            if (cacheset[i].containsKey(index)){
                if(cacheset[i].get(index) == tag)
                    res.groupno = i;
            }else
                res.emptyno = i;
        return res;
    }

    private void randomReplace(int index, int tag) {
        int no = (int)(Math.random() * (associativity - 1));
        cacheset[no].put(index, tag);
    }

    public void replacement(int index, int tag){
        switch(this.RP){
            case 0:
                randomReplace(index, tag);
                break;
            case 1:
                break;
            default:
                break;
        }
    }

    @Override
    public int load(int address){
        System.out.printf("Cache %d :", no);
        int index = address & index_mask;
        int tag = address & tag_mask;

        Res res = find(index, tag);
        String state = "load complete!\n";
        int cycle = latency[no];
        if(res.groupno == -1){
            cycle += storage.load(address);
            cycle += latency[no];
            if(res.emptyno == -1)
                replacement(index, tag);
            else
                cacheset[res.emptyno].put(index, tag);
            load_miss_count++;
            state = "load miss\n";
        }
        System.out.print(state);
        load_count++;
        return cycle;
    }


    @Override
    public int store(int address) {
        System.out.printf("Cache %d :", no);
        int index = address & index_mask;
        int tag = address & tag_mask;
        Res res = find(index, tag);
        int cycle = latency[no];


        String state = "store complete!\n";
        if(res.groupno == -1){
            cycle += storage.store(address);
            cycle += latency[no];
            if(res.emptyno == -1)
                replacement(index, tag);
            else
                cacheset[res.emptyno].put(index, tag);
            store_miss_count++;
            state = "store miss!\n";
        }
        System.out.print(state);

        store_count++;
        return cycle;
    }

}

