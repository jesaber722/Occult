package AES.Math;

import java.awt.*;

public class AESLibrary {

    private static GF256 s(GF256 n){
        if(!n.equals(GF256.ZERO)){
            return n.inverse();
        } else return GF256.ZERO;
    }
}
