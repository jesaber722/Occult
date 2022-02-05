package PhotoUtil;

import java.awt.*;

public class EffectLibrary {

    private static Color invertColor(Color c){
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha());
    }

    public static ColorArray invert(ColorArray input){
        ColorArray output = new ColorArray(input);
        for(int i = 0; i < output.height; i++){
            for(int j = 0; j < output.width; j++){
                output.set(i, j, invertColor(input.get(i, j)));
            }
        }
        return output;
    }

    public static ColorArray luminosity_grayScale(ColorArray input){
        ColorArray output = new ColorArray(input);
        for(int i = 0; i < output.height; i++){
            for(int j = 0; j < output.width; j++){
                Color orig = input.get(i,j);
                double value = orig.getRed() * 0.21 + orig.getGreen() * 0.72 + orig.getBlue() * 0.07;
                output.set(i, j, new Color((int) value, (int) value, (int) value, orig.getAlpha()));
            }
        }
        return output;
    }

    public static ColorArray projection(ColorArray input, Color base) {
        ColorArray output = new ColorArray(input);
        for (int i = 0; i < output.height; i++) {
            for (int j = 0; j < output.width; j++) {
                double dot_prod = input.get(i, j).getRed() * base.getRed() + input.get(i, j).getGreen() * base.getGreen()
                        + input.get(i, j).getBlue() * base.getBlue();
                double square_norm = base.getRed() * base.getRed() + base.getGreen() * base.getGreen() + base.getBlue() * base.getBlue();
                double factor = dot_prod / square_norm;
                output.set(i, j, new Color(
                        Math.min((int) (base.getRed() * factor + 0.5), 255),
                        Math.min((int)(base.getGreen() * factor + 0.5), 255),
                        Math.min((int)(base.getBlue() * factor + 0.5), 255)));
            }
        }
        return output;
    }

    private static Color lowBitInfo_color(Color c, int shift){
        int factor = 1;
        while(shift > 0){
            factor *= 2;
            shift --;
        }
        return new Color((c.getRed() * factor) % 256, (c.getGreen() * factor) % 256, (c.getBlue() * factor) % 256);
    }

    public static ColorArray lowBitInfo(ColorArray input, int shift){
        ColorArray output = new ColorArray(input);
        for(int i = 0; i < output.height; i++){
            for(int j = 0; j < output.width; j++){
                output.set(i, j, lowBitInfo_color(input.get(i, j), shift));
            }
        }
        return output;
    }
}
