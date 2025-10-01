package AES.Math;

/**
 * abstract classs representing a field
 */
public abstract class Ring<A extends Ring<A>> {

    public static GF256 ZERO_GF256 = new GF256((byte)0);

    public Ring() {

    }

    public Ring(String string) {

    }

    public static Ring getAnyNum(){
        return null;
    }

    public abstract A getAdditiveIdentity();

    public abstract A getMultiplicativeIdentity();

    public  abstract A add(A b);

    public abstract A multiply(A b);

    public abstract A negative();

    public abstract A inverse();

    public abstract A itself();

    public abstract Ring<A> fieldOver();

    public abstract A constructFromString(String str);

}
