package AES.Math;

public class Test {


    public static void main(String [] args){
        System.out.println((byte)0xF0);
        GF256 s = new GF256((byte)0xF0);
        GF256 t = new GF256((byte)0x0E);
        System.out.println("s "+s);
        System.out.println("t "+t);
        System.out.println();
        System.out.println(s.multiply(t));
        System.out.println(s.inverse());
        System.out.println(s.multiply(s.inverse()));

    }
}
