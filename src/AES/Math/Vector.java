package AES.Math;

public class Vector<A extends Ring<A>> {

    private final Ring<A>[] values;

    Vector(Ring<A>[] values){
        this.values = values;
    }

    A get(int i) {
        if(i >= values.length) {
            throw new IllegalArgumentException(i + " out of range");
        }
        return values[i].itself();
    }
}
