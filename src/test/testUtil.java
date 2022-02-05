package test;

import PhotoUtil.ColorArray;
import PhotoUtil.EffectLibrary;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class testUtil {

    public static String OUTPUT_FILE = "photo_output.png";
    public static String INPUT_FILE;
    public static void main(String [] args){
        Scanner sc = new Scanner(System.in);
        System.out.println("Input file: ");
        INPUT_FILE = sc.nextLine();
        System.out.println("Output file: ");
        OUTPUT_FILE = sc.nextLine();

        try {
            BufferedImage img = ImageIO.read(new File(INPUT_FILE));
            ColorArray input = new ColorArray(img);
            Color proj_color = Color.decode("#201010");
            BufferedImage output = EffectLibrary.lowBitInfo(input, 7).makeImage();
            //BufferedImage output = input.makeImage();
            try {
                File output_file = new File(OUTPUT_FILE);
                ImageIO.write(output, "png", output_file);
            } catch (
                    IOException var4) {
                System.out.println(var4);
            }

        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
