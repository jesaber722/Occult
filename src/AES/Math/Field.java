package AES.Math;

public abstract interface Field<A> {


    public A getAdditiveIdentity();

    public A getMultiplicativeIdentity();

    public  A add(A b);

    public A multiply(A b);

    public A negative();

    public A inverse();

    public A se();

}
