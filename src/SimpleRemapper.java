import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigInteger;

public class SimpleRemapper implements ImageRemapper{

    private static final double PHI = (Math.sqrt(5.0) + 1.0) / 2.0;
    private final long factor;
    private final BufferedImage img;
    private final int width;
    private final int trueWidth;
    private final long size;
    private final long trueSize;

    public SimpleRemapper(BufferedImage img){
        this.img = img;
        this.width = img.getWidth();
        this.trueWidth = width * 3;
        this.size = img.getHeight() * img.getWidth();
        this.trueSize = size * 3;
        long test_fact = Math.floorMod((long)(PHI*trueSize), trueSize);
        BigInteger mod = BigInteger.valueOf(trueSize);
        while(true){
            BigInteger n = BigInteger.valueOf(test_fact);
            if(mod.gcd(n).equals(BigInteger.ONE)){
                break;
            }
            test_fact --;
        }
        //System.out.println(test_fact);
        factor = test_fact;
    }

    public void write(boolean bit, int location){
        if(location >= trueSize){
            throw new IllegalArgumentException();
        }
        long fact_loc = Math.floorMod(location*factor, trueSize);
        int row = (int) (fact_loc / trueWidth);
        int col = (int) ((fact_loc % trueWidth) / 3);
        int field = (int) (fact_loc % 3);
        Color c = new Color(img.getRGB(col, row));
        int b = bit? 1 : 0;
        Color newC = Color.BLACK;
        int value;
        switch(field){
            case 0:
                value = c.getRed() & 254;
                value |= b;
                newC = new Color(value, c.getGreen(), c.getBlue());
                break;
            case 1:
                value = c.getGreen() & 254;
                value |= b;
                newC = new Color(c.getRed(), value, c.getBlue());
                break;
            case 2:
                value = c.getBlue() & 254;
                value |= b;
                newC = new Color(c.getRed(), c.getGreen(), value);
                break;
        }
        img.setRGB(col, row, newC.getRGB());
    }

    public boolean read(int location){
        if(location >= trueSize){
            throw new IllegalArgumentException();
        }
        long fact_loc = Math.floorMod(location*factor, trueSize);
        int row = (int) (fact_loc / trueWidth);
        int col = (int) ((fact_loc % trueWidth) / 3);
        int field = (int) (fact_loc % 3);
        int value = 0;
        switch(field){
            default:
            case 0:
                value = new Color(img.getRGB(col, row)).getRed() & 1;
                return value != 0;
            case 1:
                value = new Color(img.getRGB(col, row)).getGreen() & 1;
                return value != 0;
            case 2:
                value = new Color(img.getRGB(col, row)).getBlue() & 1;
                return value != 0;
        }
    }

    public long getTrueSize(){
        return trueSize;
    }
}
