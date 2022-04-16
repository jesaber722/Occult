package AES.Math;

import java.util.ArrayList;
import java.util.List;

public class AESLibrary {

    private final static Matrix<GF2> affineTransformMatrix = new Matrix<>(GF2.getSupplier(),
            "[1 0 0 0 1 1 1 1]" +
            "[1 1 0 0 0 1 1 1]" +
            "[1 1 1 0 0 0 1 1]" +
            "[1 1 1 1 0 0 0 1]" +
            "[1 1 1 1 1 0 0 0]" +
            "[0 1 1 1 1 1 0 0]" +
            "[0 0 1 1 1 1 1 0]" +
            "[0 0 0 1 1 1 1 1]"
    );

    private final static Matrix<GF2> affineTransVector = new Matrix<>(GF2.getSupplier(),
            "[1]" +
            "[1]" +
            "[0]" +
            "[0]" +
            "[0]" +
            "[1]" +
            "[1]" +
            "[0]"
    );

    private static byte s(byte n){
        GF256 ng = new GF256(n);
        if(!ng.equals(GF256.ZERO)){
            return ng.inverse().getValue();
        } else return 0;
    }

    private static byte affineTransformation(byte n){
        int n2 = n < 0? n + 256 : n;
        List<List<Ring<GF2>>> vals = new ArrayList<>();
        for(int i = 0; i < 8; i++){
            List<Ring<GF2>> row = new ArrayList<>();
            if(n2 % 2 == 1){
                row.add(GF2.ONE);
            } else {
                row.add(GF2.ZERO);
            }
            vals.add(row);
            n2 /= 2;
        }
        Matrix<GF2> inputVector = new Matrix<GF2>(vals);
        Matrix<GF2> res = affineTransformMatrix.multiply(inputVector).add(affineTransVector);
        int result = 0;
        int key = 1;
        for(int i = 0; i < 8; i ++){
            result += res.get(i, 0).itself().getValue() * key;
            key *= 2;
        }
        return (byte)result;
    }

    static byte byteSubstitution(byte n){
        return affineTransformation(s(n));
    }

    public static void main(String[] args){
        System.out.println(affineTransformation((byte)47));
        System.out.println(byteSubstitution((byte)194));
    }
}
