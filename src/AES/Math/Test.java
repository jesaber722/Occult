package AES.Math;

public class Test {


    public static void main(String [] args){
        System.out.println((byte)0xF0);
        GF256 s = new GF256((byte)0xF0);
        System.out.println(s);
    }
}
