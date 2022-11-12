package AES;

import java.util.Random;

public class FastGenerator {

    public static final byte [] DUMMY = {27, -110, -98, -8, -84, -61, 114, -9, -59, 127, 32, 1, 57, -77, 40, 55};

    private int next_count;
    private byte [] state;
    private int index;
    private byte[] quick_state;
    Random quick_rng;

    public FastGenerator() {
        state = new byte[16];
        index = 0;
        next_count = 0;
        next();
        quick_rng = new Random(537692637);
        quick_rng.nextBytes(quick_state);
    }

    private void next(){
        if(next_count == 16){
            next_count = 0;
            state = AESLibrary.encrypt_128(DUMMY, state);
        } else {
            quick_rng.nextBytes(quick_state);
            for(int i = 0; i < 16; i++) {
                state[i] = (byte)((int)state[i] ^ (int)quick_state[i]);
            }
            next_count ++;
        }
        index = 0;
    }

    public byte nextByte(){
        if(index == 16){
            next();
        }
        byte ret = state[index];
        index ++;
        return ret;
    }
    public boolean nextBoolean(){
        return nextByte() < 0;
    }

    public long nextLong(){
        long ret = 0;
        int i = 0;
        do{
            ret = ret << 8;
            byte b = nextByte();
            ret |= b < 0? b + 256 : b;
            i++;
        } while(i < 8);
        return ret;
    }

    public long nextSemiLong(){
        long ret = 0;
        int i = 0;
        do{
            ret = ret << 8;
            byte b = nextByte();
            ret |= b < 0? b + 256 : b;
            i++;
        } while(i < 6);
        return ret;
    }

    public void seed(final byte [] seed) {
        if(seed.length > 16) {
            throw new IllegalArgumentException("seed too long");
        }

        for(int i = 0; i < seed.length; i++) {
            state[i] = (byte)((int)state[i] ^ (int)seed[i]);
        }
        next();

    }

}
