package AES.Math;

import java.util.function.Supplier;

public class GF256 extends Ring<GF256> {

    private static final int AES_POLYNOMIAL = 283;
    //x^8 + x^4 + x^3 + x + 1
    private static boolean inverse_table_initialized = false;
    private static byte [] inverse_table = new byte[256];

    public static final GF256 ONE = new GF256((byte)1);
    public static final GF256 ZERO = new GF256((byte)0);
    public static final GF256 TWO = new GF256((byte)2);

    private final byte value;

    public static Supplier<GF256> getSupplier(){
        return new Supplier<GF256>() {
            @Override
            public GF256 get() {
                return new GF256((byte)0);
            }
        };
    }

    public GF256(){
        value = 0;
    }

    public GF256(String string){
        value = (byte)Integer.parseInt(string);
    }

    public GF256(byte v){
        value = v;
    }

    public Byte getValue(){
        return value;
    }


    @Override
    public GF256 getAdditiveIdentity() {
        return new GF256((byte)0);
    }

    @Override
    public GF256 getMultiplicativeIdentity() {
        return new GF256((byte)1);
    }

    public GF256 add( GF256 b) {
        return new GF256((byte)(value ^ ((int) b.getValue())));
    }

    public GF256 multiply( GF256 b) {
        int c = value;
        if(c < 0){
            c += 256;
        }
        int d = b.getValue();
        if(d < 0){
            d += 256;
        }
        int result = 0;
        int key = 1;
        for(int i = 0; i < 8; i++){
            int temp = d / key;
            if(temp % 2 != 0){
                result = result ^ (c * key);
            }
            key *= 2;
        }

        //System.out.println(result);

        // modding by polynomial

        while(result >= 256){
            int i = 512;
            int mod = AES_POLYNOMIAL;
            while(result >= i){
                i *= 2;
                mod *= 2;
            }
            result ^= mod;
            //System.out.println(result);

        }

        //System.out.println(result);

        return new GF256((byte)result);
    }


    public GF256 negative() {
        return new GF256(value);
    }


    public GF256 inverse() {
        byte a = getValue();
        if(!inverse_table_initialized){
            initialize_inverse_table();
        }
        if(a == (byte)0){
            throw new IllegalArgumentException("0 has no inverse.");
        }
        int index = a < 0? a + 256 : a;
        return new GF256(inverse_table[index]);
    }

    @Override
    public GF256 itself() {
        return this;
    }

    @Override
    public Ring<GF256> fieldOver(){ return this; }

    @Override
    public GF256 constructFromString(String str) {
        return new GF256((byte)Integer.parseInt(str));
    }

    private void initialize_inverse_table() {
        for(byte i = 1; i != 0; i++){
            boolean set = false;
            for(byte j = 1; j != 0; j++){
                GF256 a = new GF256(i);
                GF256 b = new GF256(j);
                if(a.multiply(b).equals(ONE)){
                    int index = i < 0? i + 256 : i;
                    inverse_table[index] = j;
                    set = true;
                    break;
                }
            }
            if(!set){
                throw new RuntimeException("This doesn't work. Oops.");
            }
        }
        inverse_table_initialized = true;
    }

    public boolean equals(GF256 other){
        return value == other.getValue();
    }

    public String toString(){
        if(value == 0)
            return "0";
        int copy = value < 0? value + 256 : value;
        //System.out.println(value);
        String output = "";
        for(int i = 0; i < 8; i++){
            //System.out.println("i"+i);
            //System.out.println("copy"+copy);
            //System.out.println(copy % 2);
            if(copy % 2 != 0){
                //System.out.println("here");
                if(i == 0){
                    output += "1 + ";
                } else if(i == 1){
                    output += "x + ";
                } else{
                    output += "x^"+i+" + ";
                }
            }
            if(copy != -1)
                copy /= 2;
            //if(copy < 0)
            //copy += -128;
        }
        //System.out.println(output);
        return  output.substring(0, output.length() - 3);
    }

    public static void main(String [] args){
        System.out.println(new GF256("117").inverse());
        System.out.println(new GF256("245").multiply(new GF256("70")));
    }
}
