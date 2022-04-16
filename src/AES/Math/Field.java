package AES.Math;

public abstract class Field<A extends Field<A>> {

    public static GF256 ZERO_GF256 = new GF256((byte)0);

    public Field() {

    }

    public Field(String string) {

    }

    public static Field getAnyNum(){
        return null;
    }

    public abstract A getAdditiveIdentity();

    public abstract A getMultiplicativeIdentity();

    public  abstract A add(A b);

    public abstract A multiply(A b);

    public abstract A negative();

    public abstract A inverse();

    public abstract A itself();

    public abstract Field<A> fieldOver();

    public abstract A constructFromString(String str);

}
