import static AES.Math.AESLibrary.encrypt_128;

public class DataArray {

    int [] data;
    final int bitsPer;
    final int array_size;
    final int size_in_bits;
    final int size_in_bytes;

    public static DataArray combine_DataArrays(DataArray [] arrays){
        int index = 0;
        int size = 0;
        for(DataArray d: arrays){
            size += d.size_in_bytes;
        }
        DataArray ret = new DataArray(size);
        for(DataArray d: arrays){
            for(int j = 0; j < d.size_in_bytes; j++){
                ret.writeByte(d.read_byte(j), index);
                index ++;
            }
        }
        return ret;
    }

    public static DataArray encrypt_DataArray_OFB(DataArray plaintext, byte [] key, byte [] IV){
        int encr_size = plaintext.size_in_bytes % 16 == 0? plaintext.size_in_bytes : plaintext.size_in_bytes + 16 - plaintext.size_in_bytes % 16;
        DataArray ciphertext = new DataArray(encr_size);
        byte [] stream = new byte[16];
        System.arraycopy(IV, 0, stream, 0, IV.length);
        for(int i = 0; i < encr_size; i += 16){
            stream = encrypt_128(stream, key);
            for(int j = 0; j < 16; j++){
                if(i + j < plaintext.size_in_bytes){
                    ciphertext.writeByte((byte)(stream[j] ^ plaintext.read_byte(i + j)), i + j);
                } else {
                    ciphertext.writeByte(stream[j], i + j);
                }
            }
        }
        /*
        for(int i = 0; i < ciphertext.size_in_bytes && ciphertext.size_in_bytes < 40; i++){
            System.out.println(ciphertext.read_byte(i) + " cipher");
        }
         */
        return ciphertext;
    }

    public static DataArray decrypt_DataArray_OFB(DataArray ciphertext, byte [] key, byte [] IV, int size) {
        if(IV.length > 16){
            throw new IllegalArgumentException();
        }
        if(key.length != 16) {
            throw new IllegalArgumentException();
        }
        DataArray plaintext = new DataArray(size);
        byte [] stream = new byte[16];
        System.arraycopy(IV, 0, stream, 0, IV.length);
        /*
        for(int i = 0; i < ciphertext.size_in_bytes; i++){
            System.out.println(ciphertext.read_byte(i) + " cipher");
        }
         */
        for(int i = 0; i < ciphertext.size_in_bytes; i += 16){
            stream = encrypt_128(stream, key);
            for(int j = 0; j < 16 && j + i < size; j++){
                plaintext.writeByte((byte)(stream[j] ^ ciphertext.read_byte(i + j)), i + j);
            }
        }
        return plaintext;
    }

    public static DataArray encrypt_DataArray_CBC(DataArray plaintext, byte [] key, byte [] IV){
        return null;
    }

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

    public DataArray(int numBytes){
        this.bitsPer = 32;
        this.array_size = (numBytes + 3) / 4;
        size_in_bytes = numBytes;
        size_in_bits = size_in_bytes * 8;
        //System.out.println("arr_size "+array_size);
        data = new int[array_size];
    }

    public DataArray(byte [] input){
        bitsPer = 32;
        data = new int[(input.length + 3) / 4];
        array_size = data.length;
        size_in_bytes = input.length;
        size_in_bits = input.length * 8;
        for(int i = 0; i < input.length; i++){
            writeByte(input[i], i);
        }
    }

    public byte [] read_bytes(int location, int length){
        byte [] ret = new byte[length];
        for(int i = 0; i < length; i++){
            ret[i] = read_byte(location + i);
        }
        return ret;
    }

    public void write_bytes(byte [] data, int location){
        for(int i = 0; i < data.length; i++){
            writeByte(data[i], location + i);
        }
        return;
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

    public byte [] produce_MAC(byte [] key){
        if(key == null){
            key = new byte[]{27, -110, -98, -8, -84, -61, 114, -9, -58, 127, 32, 1, 57, -77, 40, 55};
        }
        int s = size_in_bytes;
        int a = 0;
        byte [] MAC = new byte[16];
        while(s > 0){
            MAC[a] = (byte) (s % 256);
            s /= 256;
            a ++;
        }
        MAC = encrypt_128(key, MAC);
        for(int i = 0; i < size_in_bytes; i += 16) {
            for (int j = 0; j < 16 && j + i < size_in_bytes; j++) {
                MAC[j] = (byte)(MAC[j] ^ read_byte(i + j));
            }
            MAC = encrypt_128(key, MAC);
        }
        return MAC;
    }

    public byte [] toByteArray(){
        byte [] ret = new byte[size_in_bytes];
        for(int i = 0; i < size_in_bytes; i++){
            ret[i] = read_byte(i);
        }
        return ret;
    }

    public boolean equals(DataArray other){
        if(other.size_in_bytes != size_in_bytes){
            return false;
        }
        for(int i = 0; i < size_in_bytes; i++){
            if(read_byte(i) != other.read_byte(i)){
                return false;
            }
        }
        return true;
    }

    /*
    public static void main(String [] args){
        DataArray plaintext = new DataArray("hello world");
        for(int i = 0; i < plaintext.size_in_bytes; i++){
            System.out.println(plaintext.read_byte(i));
        }
        System.out.println();
        int size = plaintext.size_in_bytes;
        DataArray ciphertext = DataArray.encrypt_DataArray_OFB(plaintext, new byte[16], new byte[16]);
        DataArray plaintext_maybe = DataArray.decrypt_DataArray_OFB(ciphertext, new byte[16], new byte[16], size);
        for(int i = 0; i < plaintext.size_in_bytes; i++){
            System.out.println(plaintext_maybe.read_byte(i));
        }
    }

     */
}
