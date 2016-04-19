package cachesim;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by edward on 16-4-7.
 */

public class Test {
    private static class Instruction{
        int addres, cycle = 0;
        boolean type;



        Instruction(int addres, int cycle, boolean type) {
            this.addres = 0;
            this.addres = addres;
            this.cycle = cycle;
            this.type = type;
        }
    }


    private static class Configure{
        CacheConfigure cacheconf[] = new CacheConfigure[3];

        Configure(CacheConfigure cacheconf1, CacheConfigure cacheconf2, CacheConfigure cacheconf3) {
            cacheconf[0] = cacheconf1;
            cacheconf[1] = cacheconf2;
            cacheconf[2] = cacheconf3;
        }
    }

    private static Configure configure[][] = {
            {
//                    new Configure(new CacheConfigure(4 * 1024, 4, 1, 0, 0), null, null),
                    new Configure(new CacheConfigure(64 * 1024, 8, 1, 0, 0), null, null),
                    new Configure(new CacheConfigure(32 * 1024, 32, 4, 1, 0), null, null),
                    new Configure(new CacheConfigure(8 * 1024, 64, -1, 0, 0), null, null),
            },

            {
                    new Configure(new CacheConfigure(32 * 1024, 32, 4, 1, 0),
                            null,
                            new CacheConfigure(2 * 1024 * 1024, 128, 8, 1, 1)),
            },

            {
                    new Configure(new CacheConfigure(32 * 1024, 32, 4, 1, 0),
                            new CacheConfigure(1024, 32, -1, 1, 1),
                            new CacheConfigure(2 * 1024 * 1024, 128, 8, 1, 2))
            }
    };

    private static void run(ArrayList<Instruction> ins_list){
        for (Configure[] aConfigure : configure)
            for (Configure anAConfigure : aConfigure) runSample(ins_list, anAConfigure);
    }




    private static String []resultstr = {
            "access",
            "load",
            "store"
    };

    private static String []ratestr = {
            "all",
            "load",
            "store"
    };

    /**
     *
     * @param inslist      指令链表
     * @param configure　　对应的cache配置数组
     */
    private static void runSample(ArrayList<Instruction> inslist, Configure configure){

        Storage.setClock_cycle(0);
        Storage storages[] = new Storage[4];
        Memory memory = new Memory();
        Storage cache2 = configure.cacheconf[2] == null ? null : new Cache(configure.cacheconf[2], memory);
        Storage victimcache = configure.cacheconf[1] == null ? null : new VictimCache(configure.cacheconf[1], null);
        Cache cache1 = new Cache(configure.cacheconf[0], cache2 == null ? memory : cache2);
        cache1.setVictimcache((VictimCache) victimcache);

        storages[0] = memory;
        storages[1] = cache2;
        storages[2] = victimcache;
        storages[3] = cache1;


        System.out.printf("|----------------------------Result Report----------------------------------|\n\n");
        System.out.print("Configure:\n");
        String cachestr[] = {
                "cache1",
                "victimcache",
                "cache2"
        };
        String confstr[] = new String[3];
        for(int i = 3; i >= 1;i--) {
            if (storages[i] != null)
                confstr[3 - i] = storages[i] == null ? "null" : configure.cacheconf[3 - i].toString();
            System.out.printf("%s:\n[%s]\n\n", cachestr[3- i], confstr[3 - i]);
        }
        long total_cycles = 0;



        double access[][] = new double[3][4];
        double miss_loads[] = new double[4];
        double miss_stores[] = new double[4];
        double hit_rate[][] = new double[3][3];

        for(Instruction ins : inslist) {
            total_cycles += ins.cycle;
            if (ins.type)
                cache1.store(ins.addres);
            else
                cache1.load(ins.addres);
        }

        for(int i = 0; i < 4; i++) {
            access[1][i] = storages[i] == null ? Double.NaN : (double)storages[i].getLoad_count();
            access[2][i] = storages[i] == null ? Double.NaN : (double)storages[i].getStore_count();
            access[0][i] = access[1][i] + access[2][i];
            miss_loads[i] = storages[i] == null ? Double.NaN : (double)storages[i].getLoad_miss_count();
            miss_stores[i] = storages[i] == null ? Double.NaN : (double)storages[i].getStore_miss_count();
            if(i != 0){
                hit_rate[i - 1][0] = (access[0][i] - miss_loads[i] - miss_stores[i]) / access[0][i];
                hit_rate[i - 1][1] = (access[1][i] - miss_loads[i]) / access[1][i];
                hit_rate[i - 1][2] = (access[2][i] - miss_stores[i]) / access[2][i];
            }

            }

        System.out.printf("%-29s%-22s%-22s%-22s%-22s\n", "access numbers is shown by:", "memory", "cache2", "victimcache", "cache1");

        for(int i = 0; i < 3; i++)
            System.out.printf("the number of %-10s is [%-#20f, %-#20f, %-#20f, %-#20f]\n", resultstr[i], access[i][0],
                    access[i][1], access[i][2], access[i][3]);



        System.out.printf("%-27s%-22s%-22s%-22s\n", "hit rate is shown by:", "cache2", "victimcache", "cache1");
        for(int i = 0; i < 3; i++)
            System.out.printf("the hit rate 0f %-6s is [%-#20f, %-#20f, %-#20f]\n",
                    ratestr[i], hit_rate[0][i], hit_rate[1][i], hit_rate[2][i]);

        total_cycles += Storage.getClock_cycle();
        System.out.println("totla cycles = " + total_cycles);
        System.out.println("CPI = " + (double)total_cycles / access[0][3]);



  }


    //将字符串转换为32位int,　不能用Integer,因为超出了范围
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
        ArrayList<Instruction> inslist = new ArrayList<>();
        File dic = new File("./data");
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
            System.out.println("run " + file.getName());
            run(inslist);

        }


        }


}
