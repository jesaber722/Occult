package PhotoUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorArray {
    final int height;
    final int width;
    final int size;
    private Color[][] data;

    public ColorArray(BufferedImage img){
        this.height = img.getHeight();
        this.width = img.getWidth();
        size = height * width;
        data = new Color[height][width];
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                data[j][i] = new Color(img.getRGB(i, j));
            }
        }

    }

    public ColorArray(int width, int height){
        this.width = width;
        this.height = height;
        size = width * height;
        data = new Color[height][width];
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                data[j][i] = Color.BLACK;
            }
        }
    }

    public ColorArray(ColorArray colorArray){
        this.width = colorArray.width;
        this.height = colorArray.height;
        size = width * height;
        data = new Color[height][width];
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                data[j][i] = colorArray.data[j][i];
            }
        }
    }

    public void set(int i, int j, Color color){
        data[i][j] = color;
    }

    public Color get(int i, int j){
        return data[i][j];
    }

    public BufferedImage makeImage(){
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                image.setRGB(j, i, data[i][j].getRGB());
            }
        }
        return image;
    }
}
