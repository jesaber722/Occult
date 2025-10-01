package AES.Math;

import java.util.function.Supplier;

/**
 * implementation of the field of order two.
 */
public class GF2 extends Ring<GF2> {

    public final static GF2 ZERO = new GF2(0);
    public final static GF2 ONE = new GF2(1);

    private final int value;


    public static Supplier<GF2> getSupplier(){
        return new Supplier<GF2>() {
            @Override
            public GF2 get() {
                return new GF2(0);
            }
        };
    }

    public GF2(){
        value = 0;
    }

    public GF2(int value){
        this.value = value % 2;
    }

    public int getValue(){
        return value;
    }

    @Override
    public GF2 getAdditiveIdentity() {
        return ZERO;
    }

    @Override
    public GF2 getMultiplicativeIdentity() {
        return ONE;
    }

    @Override
    public GF2 add(GF2 b) {
        return new GF2((value + b.getValue()) % 2);
    }

    @Override
    public GF2 multiply(GF2 b) {
        return new GF2(value & b.getValue());
    }

    @Override
    public GF2 negative() {
        return this;
    }

    @Override
    public GF2 inverse() {
        if(value == 0){
            throw new IllegalArgumentException("0 has no inverse in GF2");
        }
        return this;
    }

    @Override
    public GF2 itself() {
        return this;
    }

    @Override
    public Ring<GF2> fieldOver() {
        return this;
    }

    @Override
    public GF2 constructFromString(String str) {
        return new GF2(Integer.parseInt(str));
    }
}
