import AES.AES128Generator;
import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * Maps data into an image in a pseudorandom pixel order, with no apparent pattern.
 */
public class AdvancedRemapper implements ImageRemapper{

    private final BufferedImage img;
    private final int width;
    private final int trueWidth;
    private final long size;
    private final int trueSize;
    public final int [] locationValues;

    /**
     * Create the remapper given the image and key.
     * The key is used as salt during the remapping.
     */
    public AdvancedRemapper(BufferedImage img, byte [] key){
        this.img = img;
        this.width = img.getWidth();
        this.trueWidth = width * 3;
        this.size = img.getHeight() * img.getWidth();
        long tSize = size * 3;
        if(tSize > Integer.MAX_VALUE){
            throw new RuntimeException("Cannot create");
        }
        trueSize = (int) tSize;
        //System.out.println("trueSize: " + trueSize);
        locationValues = new int[trueSize];
        System.out.print("Seeding RNG ...");
        AES128Generator rng = new AES128Generator();
        rng.seed(key);
        byte [] seed = new byte[16];
        for(int i = 0; i < trueSize; i+=16) {
            for(int j = 0; j < 16 && j + i < trueSize; j++) {
                int idx = i + j;
                int row = (int) (idx / trueWidth);
                int col = (int) ((idx % trueWidth) / 3);
                int field = (int) (idx % 3);
                byte value = 0;
                switch(field) {
                    default:
                    case 0:
                        value = (byte) (new Color(img.getRGB(col, row)).getRed() & -2); break;
                    case 1:
                        value = (byte) (new Color(img.getRGB(col, row)).getGreen() & -2); break;
                    case 2:
                        value = (byte) (new Color(img.getRGB(col, row)).getBlue() & -2); break;
                }
                seed[j] = value;
            }
            rng.seed(seed);
        }
        System.out.println(" done.");
        System.out.println("Calculating remapping ...");
        int remaining_size = trueSize;
        int [] remaining = new int[trueSize];
        for(int i = 0; i < trueSize; i++){
            remaining[i] = i;
        }
        while(remaining_size > 0){
            //System.out.println("remaining_size: "+remaining_size);
            long l = rng.nextSemiLong();
            if(remaining_size % 1000000 == 0){
                System.out.println("Current remaining size: " + remaining_size);
            }
            if(l < 0){
                l = -1 * l;
            }
            int loc = (int) (l % remaining_size);
            //System.out.println("loc: "+loc);
            int v = remaining[loc];
            remaining[loc] = remaining[remaining_size - 1];
            locationValues[trueSize - remaining_size] = v;
            remaining_size --;
        }
    }

    @Override
    public boolean read(int location) {
        if(location >= trueSize){
            throw new IllegalArgumentException();
        }
        int final_location = locationValues[location];
        int row = (int) (final_location / trueWidth);
        int col = (int) ((final_location % trueWidth) / 3);
        int field = (int) (final_location % 3);
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


    @Override
    public void write(boolean bit, int location) {
        if(location >= trueSize){
            throw new IllegalArgumentException();
        }
        int final_location = locationValues[location];
        int row = (int) (final_location / trueWidth);
        int col = (int) ((final_location % trueWidth) / 3);
        int field = (int) (final_location % 3);
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

    @Override
    public long getTrueSize() {
        return trueSize;
    }

}
