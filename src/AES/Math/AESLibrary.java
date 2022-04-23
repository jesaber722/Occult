/**
 * Functional interface for operations performed during AES encryption and decryption.
 */
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

    private final static Matrix<GF2> affineTransformMatrix = new Matrix<>(GF2.getSupplier(),
            "[0 1 0 1 0 0 1 0]" +
            "[0 0 1 0 1 0 0 1]" +
            "[1 0 0 1 0 1 0 0]" +
            "[0 1 0 0 1 0 1 0]" +
            "[0 0 1 0 0 1 0 1]" +
            "[1 0 0 1 0 0 1 0]" +
            "[0 1 0 0 1 0 0 1]" +
            "[1 0 1 0 0 1 0 0]"


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

    private static byte gal_inv(byte n){
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

    private static byte s(byte n){
        return affineTransformation(gal_inv(n));
    }

    private static byte [] subBytes(final byte [] txt){
        if(txt.length != 16){
            throw new IllegalArgumentException("Invalid block length");
        }
        byte [] ret = new byte[txt.length];
        for(int i = 0; i < txt.length; i++){
            ret[i] = s(txt[i]);
        }
        return ret;
    }

    private static byte [] shiftRows(final byte [] txt){
        if(txt.length != 16){
            throw new IllegalArgumentException("Invalid block length");
        }
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

    private static byte[] mixCol(final byte[] txt){
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
        return ret;
    }

    private static byte [] mixColumn(final byte [] txt){
        if(txt.length != 16){
            throw new IllegalArgumentException("Invalid block length");
        }
        byte [] ret = new byte[txt.length];
        for(int i = 0; i < 4; i++){
            byte [] tmp = new byte[4];
            for(int j = 0; j < 4; j++){
                tmp[j] = txt[4*i + j];
            }
            tmp = mixCol(tmp);
            for(int j = 0; j < 4; j++){
                ret[4*i + j] = tmp[j];
            }
        }
        return ret;
    }

    private static byte[] addRoundKey(final byte [] block, byte [] roundKey){
        byte [] ret = new byte[16];

        for(int i = 0; i < 16; i++){
            ret[i] = (byte)(block[i] ^ roundKey[i]);
        }
        return ret;
    }

    private static byte[] xor_words(byte [] n, byte [] m){
        if(n.length != 4 || m.length != 4){
            throw new IllegalArgumentException("Invalid word length");
        }
        byte [] ret = new byte[4];
        for(int i = 0; i < 4; i++){
            ret[i] = (byte) (n[i] ^ m[i]);
        }
        return ret;
    }

    private static byte[] g(byte [] n, byte round_constant){
        if(n.length != 4){
            throw new IllegalArgumentException("Invalid word length");
        }
        System.out.println("g input byte: ");
        hex_print(n);
        System.out.println();
        byte [] ret = new byte[4];
        ret[0] = n[1];
        ret[1] = n[2];
        ret[2] = n[3];
        ret[3] = n[0];
        for(int i = 0; i < 4; i++){
            ret[i] = s(ret[i]);
        }
        System.out.println("g middle byte: ");
        hex_print(ret);
        System.out.println();
        System.out.println("round const: " + round_constant);
        ret[0] = (byte)(ret[0] ^ round_constant);
        System.out.println("g output byte: ");
        hex_print(ret);
        System.out.println();
        return ret;
    }

    private static byte [][] generateSubkeys_128(final byte [] key){
        if(key.length != 16){
            throw new IllegalArgumentException("Invalid block length");
        }
        byte [][] subkeys = new byte[44][4];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                subkeys[i][j] = key[4*i+j];
            }
        }
        GF256 RC = GF256.ONE;

        for(int i = 4; i < 44; i++){
            if(i % 4 == 0){
                subkeys[i] = xor_words(subkeys[i - 4], g(subkeys[i - 1], RC.getValue()));
                RC = RC.multiply(GF256.TWO);
            } else {
                subkeys[i] = xor_words(subkeys[i - 4], subkeys[i - 1]);
            }
        }
        byte[][] ret = new byte[11][16];

        for(int i = 0; i < 11; i++){
            for(int j = 0; j < 16; j++){
                ret[i][j] = subkeys[4*i + j / 4][j % 4];
            }
        }
        return ret;
    }

    private static void hex_print(byte [] key){
        for(int i = 0; i < key.length; i++){
            System.out.printf("%02X", key[i]);
        }
    }

    private static void hex_print(byte key){
        System.out.printf("%02X", key);
    }

    public static byte[] encrypt_128(final byte [] block, byte [] key){
        if(block.length != 16){
            throw new IllegalArgumentException("Invalid block length");
        }
        if(key.length != 16){
            throw new IllegalArgumentException("Invalid key length");
        }

        byte [] ret = new byte[16];
        for(int i = 0; i < 16; i++){
            ret[i] = block[i];
        }

        byte[][] round_keys = generateSubkeys_128(key);
        //debug
        for(int i = 0; i < round_keys.length; i++){
            hex_print(round_keys[i]);
            System.out.println();
        }


        ret = addRoundKey(ret, round_keys[0]);
        for(int round = 1; round < 10; round ++){
            ret = subBytes(ret);
            ret = shiftRows(ret);
            ret = mixColumn(ret);
            ret = addRoundKey(ret, round_keys[round]);
        }
        ret = subBytes(ret);
        ret = shiftRows(ret);
        ret = addRoundKey(ret, round_keys[10]);

        return ret;
    }

    /*
    public static void arr_test(int [] stuff){
        stuff[2] = 444;
    }
     */

    private static int hexToInt(String str){
        int value = 0;
        for(int i = 0; i < str.length(); i++){
            value *= 16;
            switch(str.charAt(i)){
                case '0': value += 0; break;
                case '1': value += 1; break;
                case '2': value += 2; break;
                case '3': value += 3; break;
                case '4': value += 4; break;
                case '5': value += 5; break;
                case '6': value += 6; break;
                case '7': value += 7; break;
                case '8': value += 8; break;
                case '9': value += 9; break;
                case 'A': value += 10; break;
                case 'B': value += 11; break;
                case 'C': value += 12; break;
                case 'D': value += 13; break;
                case 'E': value += 14; break;
                case 'F': value += 15; break;
                default: throw new IllegalArgumentException("Invalid hex string");
            }
        }
        return value;
    }

    private static byte [] hexToByteArr(String str){
        byte [] ret = new byte[str.length() / 2];
        for(int i = 0; i < str.length(); i += 2){
            ret[i / 2] = (byte)hexToInt(str.substring(i, i + 2));
        }
        return ret;
    }

    public static void main(String[] args){
        // why won't this fucking work
        /*
        byte [] thing = hexToByteArr("54686174");
        byte [] thing2 = hexToByteArr("67204675");
        for(int i = 0; i < thing2.length; i++){
            thing2[i] = gal_inv(thing2[i]);
        }
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                System.out.print(i + "" + j + " ");
                hex_print((byte)(thing[i]^thing2[j]));
                System.out.println();
            }
        }
        if(true){
            return;
        }

         */
        //System.out.println(affineTransformation((byte)47));
        //System.out.println(byteSubstitution((byte)194));
        // hex conv test
        //System.out.println("BEGIN HEX CONV TEST");
        //System.out.println(hexToInt("8F"));

        // AES testing
        //System.out.println("BEGIN AES TEST");
        //System.out.println("s of 0x46 or 70: " + gal_inv((byte)70));
        hex_print(new byte[]{(byte) -11});
        System.out.println();
        byte [] inp = hexToByteArr("00000000000000000000000000000000");
        byte [] key = hexToByteArr("00000000000000000000000000000000");
        hex_print(inp);
        //System.out.println();
        //System.out.println();
        byte[] outp = encrypt_128(inp, key);
        System.out.println("AES Test: ");
        hex_print(outp);
        System.out.println();
        for(int i = 0; i < 16; i++){
            System.out.println(outp[i]);
        }
    }
}
