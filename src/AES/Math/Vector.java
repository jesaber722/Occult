package AES.Math;

public class Vector<A extends Field<A>> {

    private final Field<A> [] values;

    Vector(Field<A> [] values){
        this.values = values;
    }

    A get(int i) {
        if(i >= values.length) {
            throw new IllegalArgumentException(i + " out of range");
        }
        return values[i].itself();
    }
}
