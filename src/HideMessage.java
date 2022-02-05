import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class HideMessage {

    private static String INPUT_FILE_NAME;
    private static String OUTPUT_FILE_NAME;
    private static boolean FILE_ENCODE = false;


/*
    BufferedImage img = new BufferedImage(601, 601, 1);
    Graphics2D graphics2D = img.createGraphics();
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRect(0, 0, 601, 601);
    produceImage(graphics2D);

        try {
        File output = new File("output.png");
        ImageIO.write(img, "png", output);
    } catch (
    IOException var4) {
        System.out.println(var4);
    }
 */

    private static DataArray readFile(String filename){
        try{
            File file = new File(filename);
            long length = file.length();
            System.out.println("file length: " + length);
            System.out.println("filename length: " + filename.length());
            DataArray data = new DataArray((int)length + filename.length() + 1 );
            int data_location = 0;
            for(int i = 0; i < filename.length(); i++){
                data.writeByte(filename.charAt(i), data_location);
                data_location++;
            }
            byte [] file_bytes = Files.readAllBytes(Paths.get(filename));
            /*
            for(int i = 0; i < file_bytes.length; i++){
                System.out.println((char)file_bytes[i] + " : " + file_bytes[i]);
            }
             */

            data_location++;
            for(int i = 0; i < file_bytes.length; i++){
                data.writeByte(file_bytes[i], data_location);
                data_location++;
            }
            /*
            BufferedReader reader = new BufferedReader(new FileReader(file));
            char [] buff = new char[512];
            while(true){
                int num_read = reader.read(buff, 0, 256);
                if(num_read == -1){
                    break;
                } else {
                    for(int i = 0; i < num_read; i++){
                        data.writeByte(buff[i], data_location);
                        data_location++;
                    }
                    if(num_read < 256) {
                        break;
                    }

                }

             */
            return data;
        } catch (IOException e){
            e.printStackTrace();
            //reader.close();
        }
        return null;
    }


    public static void main(String [] args){
        Scanner sc = new Scanner(System.in);
        if(args.length > 0){
            INPUT_FILE_NAME = args[0];
        } else {
            System.out.println("Type the name of the file in which to hide the data:");
            INPUT_FILE_NAME = sc.nextLine();
        }
        String inp = "";
        DataArray data;
        if(args.length > 1){
            FILE_ENCODE = true;
            data = readFile(args[1]);
        } else {
            FILE_ENCODE = false;
            System.out.println("Type message you wish to hide: ");

            do {
                inp += sc.nextLine();
            } while (sc.hasNextLine());
            System.out.println("Message received:\n" + inp);
            data = new DataArray(inp);
        }
        int messageBitSize = data.size_in_bits * 8;
        System.out.println("Bits Required: " + data.size_in_bits);
        if(args.length > 2){
            OUTPUT_FILE_NAME = args[2];
        } else {
            System.out.println("Enter the output filename: ");
            OUTPUT_FILE_NAME = sc.nextLine();
        }
        sc.close();
        try {
            BufferedImage img = ImageIO.read(new File(INPUT_FILE_NAME));
            int imageCapacity = img.getWidth() * img.getHeight() * 3 - 64;
            System.out.println("Image capacity: " + imageCapacity);
            System.out.println(messageBitSize);
            if(imageCapacity < messageBitSize){
                System.out.println("Message is too large to fit into this picture.");
                return;
            }
            ImageRemapper writer = new ImageRemapper(img);
            int sizeEncode = data.size_in_bits;
            for(int i = 0; i < 32; i++){
                int n = sizeEncode & 1;
                writer.write(n != 0, i);
                sizeEncode = sizeEncode >> 1;
            }
            for(int i = 32; i < 40; i++){
                writer.write(FILE_ENCODE, i);
            }
            for(int i = 0; i < data.size_in_bits; i++){
                writer.write(data.read_bit(i), i + 64);
            }
            try {
                File output = new File(OUTPUT_FILE_NAME);
                ImageIO.write(img, "png", output);
            } catch (
                    IOException var4) {
                System.out.println(var4);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
