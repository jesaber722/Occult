/**
 * Functional interface for operations performed during AES encryption and decryption.
 */
package AES;

import AES.Math.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private final static Matrix<GF2> invAffineTransformMatrix = new Matrix<>(GF2.getSupplier(),
            "[0 0 1 0 0 1 0 1]" +
            "[1 0 0 1 0 0 1 0]" +
            "[0 1 0 0 1 0 0 1]" +
            "[1 0 1 0 0 1 0 0]" +
            "[0 1 0 1 0 0 1 0]" +
            "[0 0 1 0 1 0 0 1]" +
            "[1 0 0 1 0 1 0 0]" +
            "[0 1 0 0 1 0 1 0]"


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

    private final static Matrix<GF2> invAffineTransVector = new Matrix<>(GF2.getSupplier(),
            "[1]" +
            "[0]" +
            "[1]" +
            "[0]" +
            "[0]" +
            "[0]" +
            "[0]" +
            "[0]"
    );

    private final static Matrix<GF256> mixColumnMatrix = new Matrix<>(GF256.getSupplier(),
            "[2 3 1 1]" +
            "[1 2 3 1]" +
            "[1 1 2 3]" +
            "[3 1 1 2]"
    );


    private final static Matrix<GF256> invMixColumnMatrix = new Matrix<>(GF256.getSupplier(),
            "[14 11 13 9]" +
            "[9 14 11 13]" +
            "[13 9 14 11]" +
            "[11 13 9 14]"
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

    private static byte invAffineTransformation(byte n){
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
        Matrix<GF2> res = invAffineTransformMatrix.multiply(inputVector).add(invAffineTransVector);
        //Matrix<GF2> res = invAffineTransformMatrix.multiply(affineTransVector.add(inputVector));
        int result = 0;
        int key = 1;
        for(int i = 0; i < 8; i ++){
            result += res.get(i, 0).itself().getValue() * key;
            key *= 2;
        }
        return (byte)result;
    }

    private static boolean s_lookup_initialized = false;

    private static byte [] s_lookup = new byte[256];

    private static byte s(byte n){


        if(!s_lookup_initialized){
            for(int i = 0; i < 256; i++){
                s_lookup[i] = affineTransformation(gal_inv((byte)i));
            }
            s_lookup_initialized = true;
        }


        return s_lookup[n < 0? n + 256 : n];
        //return affineTransformation(gal_inv(n));
    }

    private static boolean inv_s_lookup_initialized = false;

    private static byte [] inv_s_lookup = new byte[256];

    private static byte inv_s(byte n){
        if(!inv_s_lookup_initialized) {
            for(int i = 0; i < 256; i++) {
                inv_s_lookup[i] = gal_inv(invAffineTransformation(n));
            }
        }
        return inv_s_lookup[n < 0? n + 256 : n];
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

    private static byte [] invSubBytes(final byte [] txt){
        if(txt.length != 16){
            throw new IllegalArgumentException("Invalid block length");
        }
        byte [] ret = new byte[txt.length];
        for(int i = 0; i < txt.length; i++){
            ret[i] = inv_s(txt[i]);
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

    private final static byte [] invShiftRows(final byte [] txt){
        if(txt.length != 16){
            throw new IllegalArgumentException("Invalid block length");
        }
        byte [] ret = new byte[txt.length];
        ret[0] = txt[0];
        ret[1] = txt[13];
        ret[2] = txt[10];
        ret[3] = txt[7];
        ret[4] = txt[4];
        ret[5] = txt[1];
        ret[6] = txt[14];
        ret[7] = txt[11];
        ret[8] = txt[8];
        ret[9] = txt[5];
        ret[10] = txt[2];
        ret[11] = txt[15];
        ret[12] = txt[12];
        ret[13] = txt[9];
        ret[14] = txt[6];
        ret[15] = txt[3];
        return ret;
    }

    /*
    private static byte[] oldMixCol(final byte[] txt){
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
     */

    private final static Matrix<GF256> topMixCol = new Matrix<>(GF256.getSupplier(),
                    "[2 3]" +
                    "[1 2]" +
                    "[1 1]" +
                    "[3 1]"
    );

    private final static Matrix<GF256> bottomMixCol = new Matrix<>(GF256.getSupplier(),
                    "[1 1]" +
                    "[3 1]" +
                    "[2 3]" +
                    "[1 2]"
    );

    private static boolean mixCol_lookup_initialized = false;

    private static byte [][] mixCol_top_lookup = new byte[65536][];

    private static byte [][] mixCol_bottom_lookup = new byte[65536][];

    /*
    static void initialize_mixCol_lookup(){
        for(int i = 0; i < 256; i++){
            for(int j = 0; j < 256; j++){
                List<List<Ring<GF256>>> vals = new ArrayList<>();
                List<Ring<GF256>> row1 = new ArrayList<>();
                row1.add(new GF256((byte)(i)));
                List<Ring<GF256>> row2 = new ArrayList<>();
                row2.add(new GF256((byte)(j)));
                vals.add(row1);
                vals.add(row2);
                Matrix<GF256> inputVector = new Matrix<>(vals);
                Matrix<GF256> top_output_matrix = topMixCol.multiply(inputVector);
                Matrix<GF256> bottom_output_matrix = bottomMixCol.multiply(inputVector);
                byte [] top_output = new byte[4];
                byte [] bottom_output = new byte[4];
                for(int k = 0; k < 4; k++){
                    top_output[k] = top_output_matrix.get(k, 0).itself().getValue();
                    bottom_output[k] = bottom_output_matrix.get(k, 0).itself().getValue();
                }
                mixCol_top_lookup[(256 * i + j) % 65536] = top_output;
                mixCol_bottom_lookup[(256 * i + j) % 65536] = bottom_output;
            }
        }
        mixCol_lookup_initialized = true;
    }
     */

    /*
    private static byte[] revisedMixCol(final byte[] txt){
        if(txt.length != 4) {
            throw new IllegalArgumentException("Invalid input length");
        }
        byte [] ret = new byte[txt.length];
        if(!mixCol_lookup_initialized) {
            initialize_mixCol_lookup();
        }

        //System.out.println((int)(256 * (txt[0] < 0? txt[0] + 256 : txt[0])) + (int)(txt[1] < 0? txt[1] + 256 : txt[1]));
        //System.out.println(256 * (txt[0] < 0? txt[0] + 256 : txt[0]));
        //System.out.println(txt[1] < 0? txt[1] + 256 : txt[1]);
        //System.out.println(txt[0]);
        //System.out.println(txt[1]);


        byte [] top = mixCol_top_lookup[(int)(256 * (txt[0] < 0? txt[0] + 256 : txt[0])) + (int)(txt[1] < 0? txt[1] + 256 : txt[1])];
        byte [] bottom = mixCol_bottom_lookup[(int)(256 * (txt[2] < 0? txt[2] + 256 : txt[2])) + (int)(txt[3] < 0? txt[3] + 256 : txt[3])];

        ret[0] = (byte)(top[0] ^ bottom[0]);
        ret[1] = (byte)(top[1] ^ bottom[1]);
        ret[2] = (byte)(top[2] ^ bottom[2]);
        ret[3] = (byte)(top[3] ^ bottom[3]);


        return ret;
    }
     */

    private static byte[] mixCol(final byte[] txt){
        if(txt.length != 4) {
            throw new IllegalArgumentException("Invalid input length");
        }
        byte [] ret = new byte[txt.length];

        //System.out.println((int)(256 * (txt[0] < 0? txt[0] + 256 : txt[0])) + (int)(txt[1] < 0? txt[1] + 256 : txt[1]));
        //System.out.println(256 * (txt[0] < 0? txt[0] + 256 : txt[0]));
        //System.out.println(txt[1] < 0? txt[1] + 256 : txt[1]);
        //System.out.println(txt[0]);
        //System.out.println(txt[1]);

        int top_index = (int)(256 * (txt[0] < 0? txt[0] + 256 : txt[0])) + (int)(txt[1] < 0? txt[1] + 256 : txt[1]);
        byte [] top = mixCol_top_lookup[top_index];
        if(top == null) {
            //System.out.println(topMixCol);
            List<List<Ring<GF256>>> vals = new ArrayList<>();
            List<Ring<GF256>> row1 = new ArrayList<>();
            row1.add(new GF256(txt[0]));
            List<Ring<GF256>> row2 = new ArrayList<>();
            row2.add(new GF256(txt[1]));
            vals.add(row1);
            vals.add(row2);
            Matrix<GF256> inputVector = new Matrix<>(vals);
            Matrix<GF256> top_output_matrix = topMixCol.multiply(inputVector);
            byte [] top_output = new byte[4];
            for(int k = 0; k < 4; k++){
                top_output[k] = top_output_matrix.get(k, 0).itself().getValue();
            }
            mixCol_top_lookup[top_index] = top_output;
        }
        top = mixCol_top_lookup[top_index];

        int bottom_index = (int)(256 * (txt[2] < 0? txt[2] + 256 : txt[2])) + (int)(txt[3] < 0? txt[3] + 256 : txt[3]);
        byte [] bottom = mixCol_bottom_lookup[bottom_index];
        if(bottom == null) {
            List<List<Ring<GF256>>> vals = new ArrayList<>();
            List<Ring<GF256>> row1 = new ArrayList<>();
            row1.add(new GF256(txt[2]));
            List<Ring<GF256>> row2 = new ArrayList<>();
            row2.add(new GF256(txt[3]));
            vals.add(row1);
            vals.add(row2);
            Matrix<GF256> inputVector = new Matrix<>(vals);
            Matrix<GF256> bottom_output_matrix = bottomMixCol.multiply(inputVector);
            byte [] bottom_output = new byte[4];
            for(int k = 0; k < 4; k++){
                bottom_output[k] = bottom_output_matrix.get(k, 0).itself().getValue();
            }
            mixCol_bottom_lookup[bottom_index] = bottom_output;
        }
        bottom = mixCol_bottom_lookup[bottom_index];

        //System.out.println(top[3]);
        //System.out.println(bottom[3]);

        ret[0] = (byte)(top[0] ^ bottom[0]);
        ret[1] = (byte)(top[1] ^ bottom[1]);
        ret[2] = (byte)(top[2] ^ bottom[2]);
        ret[3] = (byte)(top[3] ^ bottom[3]);


        return ret;
    }

    private static byte[] invMixCol(final byte[] txt){
        byte [] ret = new byte[txt.length];
        List<List<Ring<GF256>>> inp = new ArrayList<>();
        for(int i = 0; i < txt.length; i++){
            List<Ring<GF256>> row = new ArrayList<>();
            row.add(new GF256(txt[i]));
            inp.add(row);
        }
        Matrix<GF256> inputVector = new Matrix<>(inp);
        Matrix<GF256> outputVector = invMixColumnMatrix.multiply(inputVector);
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

    private static byte [] invMixColumn(final byte [] txt){
        if(txt.length != 16){
            throw new IllegalArgumentException("Invalid block length");
        }
        byte [] ret = new byte[txt.length];
        for(int i = 0; i < 4; i++){
            byte [] tmp = new byte[4];
            for(int j = 0; j < 4; j++){
                tmp[j] = txt[4*i + j];
            }
            tmp = invMixCol(tmp);
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
        //System.out.println("g input byte: ");
        //hex_print(n);
        //System.out.println();
        byte [] ret = new byte[4];
        ret[0] = n[1];
        ret[1] = n[2];
        ret[2] = n[3];
        ret[3] = n[0];
        for(int i = 0; i < 4; i++){
            ret[i] = s(ret[i]);
        }
        //System.out.println("g middle byte: ");
        //hex_print(ret);
        //System.out.println();
        //System.out.println("round const: " + round_constant);
        ret[0] = (byte)(ret[0] ^ round_constant);
        //System.out.println("g output byte: ");
        //hex_print(ret);
        //System.out.println();
        return ret;
    }

    private static boolean mul_2_initialized = false;

    private static byte [] mul_2_lookup = new byte[256];

    private static byte galois_multiply_by_two(final byte b){
        if(!mul_2_initialized){
            for(int i = 0; i < 256; i++){
                mul_2_lookup[i] = new GF256((byte)i).multiply(GF256.TWO).getValue();
            }
            mul_2_initialized = true;
        }


        return mul_2_lookup[b < 0? b + 256 : b];
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

    private static byte [][] revised_generateSubkeys_128(final byte [] key){
        if(key.length != 16){
            throw new IllegalArgumentException("Invalid block length");
        }
        byte [][] subkeys = new byte[44][4];
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                subkeys[i][j] = key[4*i+j];
            }
        }
        byte RC = 1;

        for(int i = 4; i < 44; i++){
            if(i % 4 == 0){
                subkeys[i] = xor_words(subkeys[i - 4], g(subkeys[i - 1], RC));
                RC = galois_multiply_by_two(RC);
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

    public static void hex_print(byte [] key){
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
            //hex_print(round_keys[i]);
            //System.out.println();
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

    public static byte[] revised_encrypt_128(final byte [] block, byte [] key){
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

        byte[][] round_keys = revised_generateSubkeys_128(key);
        //debug
        for(int i = 0; i < round_keys.length; i++){
            //hex_print(round_keys[i]);
            //System.out.println();
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

    public static byte[] decrypt_128(final byte [] block, byte [] key){
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
            //hex_print(round_keys[i]);
            //System.out.println();
        }

        ret = addRoundKey(ret, round_keys[10]);
        ret = invShiftRows(ret);
        ret = invSubBytes(ret);

        for(int round = 9; round >= 1; round --){
            ret = addRoundKey(ret, round_keys[round]);
            ret = invMixColumn(ret);
            ret = invShiftRows(ret);
            ret = invSubBytes(ret);
            ret = addRoundKey(ret, round_keys[0]);

        }
        //

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
                case 'a':
                case 'A': value += 10; break;
                case 'b':
                case 'B': value += 11; break;
                case 'c':
                case 'C': value += 12; break;
                case 'd':
                case 'D': value += 13; break;
                case 'e':
                case 'E': value += 14; break;
                case 'f':
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
        // s inv testing
        //hex_print(decrypt_128(encrypt_128(hexToByteArr("00000000000000000000000000000000"), hexToByteArr("00000000000000000000000000000000")), hexToByteArr("00000000000000000000000000000000")));


        /*
        // improved generate_subkeys

        Random rng = new Random(0);

        for(int i = 0; i < 1000000; i++){
            byte[] input = new byte[16];
            rng.nextBytes(input);
            byte [][] orig = generateSubkeys_128(input);
            byte [][] revised = revised_generateSubkeys_128(input);
            /*
            System.out.println("Orig: ");
            for(int j = 0; j < orig.length; j++){
                hex_print(orig[j]);
                System.out.println();
            }
            System.out.println("Revised: ");
            for(int j = 0; j < revised.length; j++){
                hex_print(revised[j]);
                System.out.println();
            }
            System.out.println();
            */ /*
            if(orig.length != revised.length) {
                throw new RuntimeException("Length wrong");
            }
            for(int j = 0; j < orig.length; j++){
                for(int k = 0; k < orig[j].length; k++){
                    if(orig[j][k] != revised[j][k]){
                        throw new RuntimeException("Value wrong");
                    }
                }
            }
        }
        System.out.println("good");
*/

/*

        // memoized mixCol testing

        Random rng = new Random(0);

        for(int i = 0; i < 1000000; i++){
            byte[] input = new byte[4];
            rng.nextBytes(input);
            byte [] orig = mixCol(input);
            byte [] revised = revisedMixCol(input);
            hex_print(orig);
            System.out.println();
            hex_print(revised);
            System.out.println();
            if(orig.length != revised.length) {
                throw new RuntimeException("Length wrong");
            }
            for(int j = 0; j < 4; j++){
                if(orig[j] != revised[j]) {
                    throw new RuntimeException("Value wrong");
                }
            }
            System.out.println();
        }
        System.out.println("good");

 */

/*
        // testing full AES accuracy

        Random rng = new Random(0);
        byte[] input = new byte[16];
        byte[] key = new byte[16];

        int ROUNDS = 200000;

        for(int i = 0; i < ROUNDS; i++){
            rng.nextBytes(input);
            rng.nextBytes(key);
            byte [] old = revised_encrypt_128(input, key);
            byte [] revised = encrypt_128(input, key);

            for(int j = 0; j < 16; j++) {
                if(old[j] != revised[j]) {
                    throw new RuntimeException("bad value");
                }
            }
        }
        System.out.println("good");
        */



        // testing speedup from revision

        Random rng = new Random(0);
        byte[] input = new byte[16];
        byte[] key = new byte[16];

        int ROUNDS = 2000000;

        long end = Instant.now().toEpochMilli();
        long start = Instant.now().toEpochMilli();

        for(int i = 0; i < ROUNDS; i++){
            rng.nextBytes(input);
            rng.nextBytes(key);
            revised_encrypt_128(input, key);
        }

        start = Instant.now().toEpochMilli();
        for(int i = 0; i < ROUNDS; i++){
            rng.nextBytes(input);
            rng.nextBytes(key);
            revised_encrypt_128(input, key);
        }
        end = Instant.now().toEpochMilli();

        long old = end - start;

        rng = new Random(0);

        start = Instant.now().toEpochMilli();
        for(int i = 0; i < ROUNDS; i++){
            rng.nextBytes(input);
            rng.nextBytes(key);
            revised_encrypt_128(input, key);
        }
        end = Instant.now().toEpochMilli();

        long revised = end - start;


        System.out.println("old: " + old);
        System.out.println("old in seconds: " + old / 1000);
        System.out.println("new: " + revised);
        System.out.println("new in seconds: " + revised / 1000);

        System.out.println("Portion saved: " + (1 - ((double)revised/(double)old)));


        if(true){
            return;
        }
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
        byte [] inp = hexToByteArr("6675636B000000000000000000000000");
        byte [] kkey = hexToByteArr("00000000000000000000000000000000");
        hex_print(inp);
        //System.out.println();
        //System.out.println();
        byte[] outp = encrypt_128(inp, kkey);
        System.out.println("AES Test: ");
        hex_print(outp);
        System.out.println();
        for(int i = 0; i < 16; i++){
            System.out.println(outp[i]);
        }
    }
}
