package AES.Math;

import java.math.BigInteger;
import java.util.function.Supplier;

public class IntRing extends Ring<IntRing> {

    public final static IntRing ZERO = new IntRing(BigInteger.ZERO);
    public final static IntRing ONE = new IntRing(BigInteger.ONE);

    private final BigInteger value;

    public static Supplier<IntRing> getSupplier(){
        return new Supplier<IntRing>() {
            @Override
            public IntRing get() {
                return new IntRing(BigInteger.ZERO);
            }
        };
    }

    public IntRing(){
        value = BigInteger.ZERO;
    }

    public IntRing(BigInteger value){
        this.value = value;
    }

    public IntRing (long value){
        this.value = new BigInteger(Long.toString(value));
    }

    public BigInteger getValue(){
        return value;
    }

    @Override
    public IntRing getAdditiveIdentity() {
        return new IntRing(BigInteger.ZERO);
    }

    @Override
    public IntRing getMultiplicativeIdentity() {
        return new IntRing(BigInteger.ONE);
    }

    @Override
    public IntRing add(IntRing b) {
        return new IntRing(value.add(b.getValue()));
    }

    @Override
    public IntRing multiply(IntRing b) {
        return new IntRing(value.multiply(b.getValue()));
    }

    @Override
    public IntRing negative() {
        return new IntRing(BigInteger.ZERO.subtract(value));
    }

    @Override
    public IntRing inverse() {
        if(value.equals(BigInteger.ONE) || value.equals(BigInteger.ZERO.subtract(BigInteger.ONE)))
            return this;
        throw new IllegalArgumentException(this.toString() + " is not invertible in IntRing");
    }

    @Override
    public IntRing itself() {
        return this;
    }

    @Override
    public Ring<IntRing> fieldOver() {
        return this;
    }

    @Override
    public IntRing constructFromString(String str) {
        return new IntRing(new BigInteger(str));
    }

    public String toString(){
        return value.toString();
    }
}
