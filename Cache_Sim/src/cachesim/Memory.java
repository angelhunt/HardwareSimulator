package cachesim;

/**
 * Created by edward on 16-4-7.
 */
public class Memory extends Storage {
    @Override
    public Res load(int address) {
        //int cycle = 100;
        clock_cycle += 100;
        load_count++;
        return new Res();
    }

    @Override
    public Res store(int address) {
        clock_cycle += 100;
        store_count++;
        return new Res();
    }
}
