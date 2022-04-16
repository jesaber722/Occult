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

    private final static Matrix<GF256> mixColumnMatrix = new Matrix<>(GF256.getSupplier(),
            "[2 3 1 1]" +
            "[1 2 3 1]" +
            "[1 1 2 3]" +
            "[3 1 1 2]"
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

    private static byte byteSubstitution(byte n){
        return affineTransformation(s(n));
    }

    static byte [] subBytes(byte [] txt){
        byte [] ret = new byte[txt.length];
        for(int i = 0; i < txt.length; i++){
            ret[i] = byteSubstitution(txt[i]);
        }
        return ret;
    }

    static byte [] shiftRows(byte [] txt){
        byte [] ret = new byte[txt.length];
        ret[0] = txt[0];
        ret[1] = txt[5];
        ret[2] = txt[10];
        ret[3] = txt[15];
        ret[4] = txt[4];
        ret[5] = txt[9];
        ret[6] = txt[14];
        ret[7] = txt[3];
        ret[8] = txt[8];
        ret[9] = txt[13];
        ret[10] = txt[2];
        ret[11] = txt[7];
        ret[12] = txt[12];
        ret[13] = txt[1];
        ret[14] = txt[6];
        ret[15] = txt[11];
        return ret;
    }

    private static void mixCol(byte[] txt){
        byte [] ret = new byte[txt.length];
        List<List<Ring<GF256>>> inp = new ArrayList<>();
        for(int i = 0; i < txt.length; i++){
            List<Ring<GF256>> row = new ArrayList<>();
            row.add(new GF256(txt[i]));
            inp.add(row);
        }
        Matrix<GF256> inputVector = new Matrix<>(inp);
        Matrix<GF256> outputVector = mixColumnMatrix.multiply(inputVector);
        for(int i = 0; i < ret.length; i++){
            ret[i] = outputVector.get(i, 0).itself().getValue();
        }
    }

    static byte [] mixColumn(byte [] txt){
        byte [] ret = new byte[txt.length];
        // TODO
        return null;
    }

    public static void main(String[] args){
        System.out.println(affineTransformation((byte)47));
        System.out.println(byteSubstitution((byte)194));
    }
}
