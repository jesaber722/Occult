public class DataArray {

    int [] data;
    final int bitsPer;
    final int array_size;
    final int size_in_bits;
    final int size_in_bytes;

    public DataArray(String string) {
        bitsPer = 8;
        data = new int[string.length()];
        array_size = string.length();
        size_in_bits = array_size * bitsPer;
        size_in_bytes = array_size * bitsPer / 8;
        for(int i = 0; i < string.length(); i++){
            char c = string.charAt(i);
            int n = (int) c;
            data[i] = n;
        }
    }
    public DataArray(int bitsPer, int size){
        this.bitsPer = bitsPer;
        this.array_size = size;
        this.size_in_bits = size * bitsPer;
        this.size_in_bytes = size * bitsPer / 8;
        data = new int[size];
    }

    public DataArray(int numBytes){
        this.bitsPer = 32;
        this.array_size = (numBytes + 3) / 4;
        size_in_bytes = numBytes;
        size_in_bits = size_in_bytes * 8;
        //System.out.println("arr_size "+array_size);
        data = new int[array_size];
    }

    public byte read_byte(int location){
        if(bitsPer % 8 == 0){
            int bytesPer = bitsPer / 8;
            int index = location / bytesPer;
            int shift = location % bytesPer;
            int key = 255;
            int i = shift;
            while(i > 0){
                key *= 256;
                i --;
            }
            int d = (data[index] & key);
            i = shift;
            while(i > 0){
                d = d >>> 8;
                i --;
            }
            //System.out.println("char: "+(int)d);
            return (byte)d;
        } else {
            return '\0';
        }
    }

    public void writeByte(char c, int location){
        if(bitsPer % 8 == 0){
            int bytesPer = bitsPer / 8;
            int index = location / bytesPer;
            int shift = location % bytesPer;
            int key = 255;
            int val = (int) c;
            if(val < 0){
                val += 256;
            }
            while(shift > 0){
                key *= 256;
                val *= 256;
                shift --;
            }
            //System.out.println("wr val:"+val);
            //System.out.println("wr key:"+key);
            key = ~key;
            int p = data[index];
            p = p & key;
            p = p | val;
            data[index] = p;
        } else{
            return;
        }
    }

    public void writeByte(byte c, int location){
        if(bitsPer % 8 == 0){
            int bytesPer = bitsPer / 8;
            int index = location / bytesPer;
            int shift = location % bytesPer;
            int key = 255;
            int val = (int) c;
            if(val < 0){
                val += 256;
            }
            while(shift > 0){
                key *= 256;
                val *= 256;
                shift --;
            }
            //System.out.println("wr val:"+val);
            //System.out.println("wr key:"+key);
            key = ~key;
            int p = data[index];
            p = p & key;
            p = p | val;
            data[index] = p;
        } else{
            return;
        }
    }

    public boolean read_bit(int location){
        int index = location / bitsPer;
        int shift = location % bitsPer;
        int key = 1;
        while(shift > 0){
            key = key << 1;
            shift --;
        }
        return (data[index] & key) != 0;
    }

    public void write_bit(boolean b, int location){
        int index = location / bitsPer;
        int shift = location % bitsPer;
        int key = 1;
        int val = b? 1: 0;
        while(shift > 0){
            key = key << 1;
            val = val << 1;
            shift --;
        }
        key = ~key;
        int p = data[index];
        p = p & key;
        p = p | val;
        data[index] = p;
    }
}
