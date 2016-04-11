package org.mhj.cachesim;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by edward on 16-4-7.
 */

public class Test {
    private static class Instruction{
        protected int addres = 0, cycle = 0;
        protected boolean type;

        public Instruction(int addres, int cycle, boolean type) {
            this.addres = addres;
            this.cycle = cycle;
            this.type = type;
        }
    }

    private static void run(ArrayList<Instruction> inslist){
        Cache cache = new Cache();
        long total_cycles = 0;
       /* int access[] = new int[4];
        int loads[] = new int[4];
        int stores[] = new int[4];
       */
        int access[][] = new int[4][4];
        int miss_loads[] = new int[4];
        int miss_stores[] = new int[4];
        double cache_hit_rate;
        double cache_hit_load_rate;
        double cache_hit_store_rate;
        double cpi;
        for(Instruction ins : inslist) {
            int cycle = 0;
            if (ins.type)
                cycle = cache.store(ins.addres);
            else
                cycle = cache.load(ins.addres);
            total_cycles += cycle + ins.cycle;

            access[1][0] += cache.getLoad_count();
            access[2][0] += cache.getStore_count();
            access[0][0] = access[1][0] + access[2][0];
            miss_loads[0] += cache.getLoad_miss_count();
            miss_stores[0] += cache.getStore_miss_count();
        }
        String []resultstr = {"number of access is [%d, %d, %d, %d]\n",
                "number of load is [%d, %d, %d, %d]\n",
                "number of store is [%d, %d, %d, %d]\n"};
        for(int i = 0; i < 3; i++)
            System.out.printf(resultstr[i], access[i][0], access[i][1], access[i][2], access[i][3]);

        int accesstimes = 0, loads = 0, stores = 0, loadmiss = 0, storemiss = 0;
        for(int i = 0; i < 4; i++) {
            accesstimes += access[i][0];
            loads +=access[i][1];
            stores += access[i][2];
            loadmiss += miss_loads[i];
            storemiss += miss_stores[i];
        }

        System.out.printf("cache hit rate = %f\n",
                (double)(accesstimes - loadmiss - storemiss) / (double)accesstimes);
        System.out.printf("cache hit rate for load = %f\n",
                (double)(loads - loadmiss) / (double)loads);
        System.out.printf("cache hit rate = %f\n",
                (double)(stores - storemiss) / (double)stores);
        System.out.println("cpu time is " + total_cycles);


  }
    private static int toHex(String s){
        int value = 0;
        for(int i = 0; i < 8; i++){
            char c = s.charAt(i);
            int t = 0;
            if(c >= '0' && c <= '9')
                t = c - '0';
            if(c >= 'a' && c <= 'f')
                t = c - 'a' + 10;
            t = t <<(32 - (i + 1) * 4);
            value = value | t;
        }
        return value;
    }

    public static void main(String [] args) throws FileNotFoundException {
        anObtain("B", "ABBA");
        ArrayList<Instruction> inslist = new ArrayList<>();
        File dic = new File("./src/src/org/mhj/cachesim/data");
        File[] files = dic.listFiles();
        System.out.println(Integer.toHexString(Integer.MIN_VALUE));
        assert files != null;
        for(File file : files){
            Scanner input = new Scanner(file);
            inslist.clear();
            while(input.hasNext()){
                String line = input.nextLine();
                String tokens[] = line.split(" ");
                Test.Instruction ins = new Test.Instruction(toHex(tokens[1].substring(2)),
                Integer.parseInt(tokens[2]), tokens[0].equals("s"));
                inslist.add(ins);

            }
            run(inslist);

        }


        }
    private static String anObtain(String initial, String target){
        helper(new StringBuilder(initial), target);
        return "";
    }
    private static boolean helper(StringBuilder initial, String target){
        if(initial.length() == target.length()){
            boolean res = true;
            for(int i = 0; i < initial.length(); i++)
                if(initial.charAt(i) != target.charAt(i)){
                    res = false;
                    break;
                }
            return res;
        }

        initial.append("A");
        if(helper(initial, target))
            return true;
        initial.deleteCharAt(initial.length() - 1);

        initial.reverse();
        initial.append("B");
        if(helper(initial, target))
            return true;

        initial.deleteCharAt(initial.length() - 1);
        initial.reverse();

        return false;

    }
}
