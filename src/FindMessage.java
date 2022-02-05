import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.Scanner;

public class FindMessage {

    public static void write_file(String filename, DataArray data, int location){
        try {
            System.out.println("filename: "+filename);
            Path path = Paths.get(filename);
            byte [] data_bytes = new byte[data.size_in_bytes - location];
            for(int i = location; i < data.size_in_bytes; i++){
                data_bytes[i - location] = data.read_byte(i);
            }
            Files.write(path, data_bytes, StandardOpenOption.CREATE);
            /*
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            char [] buff = new char[256];
            while(location != data.size_in_bytes){
                int i = 0;
                while(i < 256) {
                    buff[i] = (char)data.read_byte(location);
                    i++;
                    location++;
                    if(location == data.size_in_bytes){
                        break;
                    }
                }
                writer.write(buff, 0, i);
            }
            writer.flush();
            writer.close();

             */
        } catch(IOException e){
            e.printStackTrace();
        }
    }


    public static void main(String [] args){
        String OUTPUT_FILE_NAME;
        try{
            if(args.length > 0){
                OUTPUT_FILE_NAME = args[0];
            } else{
                Scanner sc = new Scanner(System.in);
                System.out.println("Type the filename to uncover: ");
                    OUTPUT_FILE_NAME = sc.nextLine();
                    sc.close();
                }

            BufferedImage img = ImageIO.read(new File(OUTPUT_FILE_NAME));
            ImageRemapper reader = new ImageRemapper(img);
            int size = 0;
            for(int i = 31; i >= 0; i--){
                int val = reader.read(i)? 1: 0;
                size |= val;
                if(i != 0)
                    size = size << 1;
            }
            //System.out.println("size " + size);
            DataArray message = new DataArray( size / 8);
            for(int i = 0; i < size; i++){
                //System.out.println("i"+i);
                message.write_bit(reader.read(i + 64), i);
            }
            /*
            for(int i = message.size_in_bytes - 16; i < message.size_in_bytes; i++){
                System.out.println(message.read_byte(i) + " : " + (int)message.read_byte(i));
            }

             */
            //System.out.println("first: " + message.data[0]);
            boolean file_decode = false;
            for(int i = 39; i >= 32; i--){
                if(reader.read(i)){
                    file_decode = true;
                    break;
                }
            }
            if(!file_decode) {
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < message.size_in_bytes; i++) {
                    char c = (char) message.read_byte(i);
                    result.append(c);
                }
                System.out.println(result);
            } else {
                StringBuilder filename_builder = new StringBuilder();
                int i = 0;
                while(true){
                    char c = (char) message.read_byte(i);
                    if(c == '\0'){
                        i++;
                        break;
                    }
                    filename_builder.append(c);
                    i++;
                }
                String filename = filename_builder.toString();
                write_file(filename, message, i);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
