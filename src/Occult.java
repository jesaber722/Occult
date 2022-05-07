import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Calendar;
import java.util.Scanner;

import static AES.Math.AESLibrary.encrypt_128;

public class Occult {

    private static final int NUM_IV_BYTES = 8;
    private static final int TRUE_NUM_SIZE_BYTES = 4;
    private static final int NUM_SIZE_BYTES = 16;
    private static final int NUM_MAC_BYTES = 16;

    private static byte [] produce_key(String password) {
        byte[] buf = new byte[16];
        byte[] dummy = {27, -110, -98, -8, -84, -61, 114, -9, -58, 127, 32, 1, 57, -77, 40, 55};
        // randomly chosen ^^^
        for (int i = 0; i < password.length(); i += 16) {
            //System.out.println(i + "");
            for (int j = 0; j < 16 && i + j < password.length(); j++) {
                buf[j] = (byte)(buf[j] ^ password.charAt(i + j));
            }
            buf = encrypt_128(dummy, buf);
        }
        buf = encrypt_128(dummy, buf);
        return buf;
    }

    private static void reveal(String png_name, String out_name, byte [] key){
        try{
            BufferedImage img = ImageIO.read(new File(png_name));
            ImageRemapper reader = new ImageRemapper(img);
            DataArray IV = new DataArray(NUM_IV_BYTES);
            DataArray MAC = new DataArray(NUM_MAC_BYTES);
            DataArray size_data = new DataArray(NUM_SIZE_BYTES);

            for(int i = 0; i < 8 *  NUM_IV_BYTES; i++){
                IV.write_bit(reader.read(i), i);
            }
            for(int i = 0; i < 8 * NUM_MAC_BYTES; i++){
                MAC.write_bit(reader.read(i + 8 * NUM_IV_BYTES), i);
            }

            for(int i = 0; i < 8 * NUM_SIZE_BYTES; i++){
                size_data.write_bit(reader.read(i + 8 * (NUM_IV_BYTES + NUM_MAC_BYTES)), i);
            }
            size_data = DataArray.decrypt_DataArray_OFB(size_data, key, IV.toByteArray(), TRUE_NUM_SIZE_BYTES);
            int size = 0;
            for(int i = size_data.size_in_bytes - 1; i >= 0; i--){
                int n = size_data.read_byte(i);
                if(n < 0){
                    n += 256;
                }
                size += n;
                if(i != 0) {
                    size = size << 8;
                }
            }

            if(size > reader.getTrueSize() / 8 || size < 0){
                System.out.println("Could not verify integrity of the data.");
                return;
            }
            //System.out.println(size);
            int encr_size = size % 16 == 0? size : size + 16 - size % 16;
            DataArray file_name_and_data = new DataArray(encr_size);
            for(int i = 0; i < 8 * encr_size; i++){
                file_name_and_data.write_bit(reader.read(i + 8 * (NUM_IV_BYTES + NUM_MAC_BYTES + NUM_SIZE_BYTES)), i);
            }
            file_name_and_data = DataArray.decrypt_DataArray_OFB(file_name_and_data, key, IV.toByteArray(), size);
            if(!new DataArray(file_name_and_data.produce_MAC(key)).equals(MAC)){
                System.out.println("Could not verify integrity of the data.");
                return;
            }
            //System.out.println("We're in business.");
            int split_index = 0;
            while(file_name_and_data.read_byte(split_index) != (byte)0)
                split_index++;
            byte [] filename_data = new byte[split_index];
            for(int i = 0; i < split_index; i++){
                filename_data[i] = file_name_and_data.read_byte(i);
            }
            split_index ++;
            byte [] file_data = new byte[file_name_and_data.size_in_bytes - split_index];
            for(int i = 0; i < file_data.length; i++){
                file_data[i] = file_name_and_data.read_byte(split_index + i);
            }
            if(out_name == null) {
                out_name = new String(filename_data);
            } else {
                System.out.println("Original filename: " + new String(filename_data));
            }
            Files.write(Paths.get(out_name), file_data, StandardOpenOption.CREATE);

        } catch(IOException e){
            e.printStackTrace();
        }
        return;
    }

    private static void hide(String png_name, String hide_name, String out_name, byte [] key){
        try {
            // read the file data
            DataArray file_name_and_data;
            {
                byte[] file_data = Files.readAllBytes(Paths.get(hide_name));
                byte[] filename_data = new File(hide_name).getName().getBytes();
                file_name_and_data = new DataArray(file_data.length + filename_data.length + 1);
                file_name_and_data.write_bytes(filename_data, 0);
                file_name_and_data.writeByte((byte)0, filename_data.length);
                file_name_and_data.write_bytes(file_data, filename_data.length + 1);
            }
            // get the IV
            long time = Calendar.getInstance().getTimeInMillis();
            //System.out.println(time);
            DataArray IV = new DataArray(NUM_IV_BYTES);
            for(int i = 0; i < IV.size_in_bytes; i++){
                IV.writeByte((byte)(time % 256), i);
                //System.out.println(IV[i] + " IV"+i);
                time = time >>> 8;
            }
            /*
            for(int i = 0; i < IV.size_in_bytes; i++){
                System.out.println(IV.read_byte(i));
            }
             */
            // get the size
            int orig_size = file_name_and_data.size_in_bytes;
            //System.out.println(orig_size);
            DataArray size_data = new DataArray(TRUE_NUM_SIZE_BYTES);
            for(int i = 0 ; i < TRUE_NUM_SIZE_BYTES; i++){
                size_data.writeByte((byte)(orig_size % 256), i);
                orig_size = orig_size >>> 8;
            }
            DataArray MAC = new DataArray(file_name_and_data.produce_MAC(key));
            if(key != null) {
                size_data = DataArray.encrypt_DataArray_OFB(size_data, key, IV.toByteArray());
                file_name_and_data = DataArray.encrypt_DataArray_OFB(file_name_and_data, key, IV.toByteArray());
            }
            DataArray all_data = DataArray.combine_DataArrays(new DataArray[]{IV, MAC, size_data, file_name_and_data});
            BufferedImage img = ImageIO.read(new File(png_name));
            ImageRemapper remapper = new ImageRemapper(img);
            System.out.println("Size of data to hide: " + all_data.size_in_bytes);
            System.out.println("Capacity of " + png_name + ": " + (remapper.getTrueSize() / 8));
            if((remapper.getTrueSize() / 8) < all_data.size_in_bytes){
                System.out.println("Data does not fit into the picture.");
                return;
            }
            for(int i = 0; i < all_data.size_in_bits; i++){
                remapper.write(all_data.read_bit(i), i);
            }
            if(out_name == null){
                out_name = "output.png";
            }
            File output = new File(out_name);
            ImageIO.write(img, "png", output);
        } catch (IOException e){
            e.printStackTrace();
        }
        return;
    }

    private static void usage(){
        System.out.println("Occult picture.png [--hide file] [--out file] [--raw]");
    }

    public static void main(String [] args) {
        String mode = null;
        String raw = null;
        String IV = null;
        String hide_name = null;
        String out_name = null;
        String png_name = null;

        String previous = null;
        for(String arg : args){
            if(previous == null){
                if(arg.equals("--raw")){
                    if(raw != null){
                        usage();
                        return;
                    }
                    raw = "y";
                } else if(arg.equals("--hide")){
                    if(hide_name != null){
                        usage();
                        return;
                    }
                    previous = arg;
                } else if(arg.equals("--out")){
                    if(out_name != null){
                        usage();
                        return;
                    }
                    previous = arg;
                } else if(arg.charAt(0) == '-'){
                    usage();
                    return;
                } else {
                    if(png_name != null){
                        usage();
                        return;
                    }
                    File file = new File(arg);
                    if(!file.exists()){
                        System.out.println("png file not found");
                        return;
                    }
                    png_name = arg;
                }
            } else {
                if(previous.equals("--hide")){
                    File file = new File(arg);
                    if(!file.exists()){
                        System.out.println("File to hide not found");
                        return;
                    }
                    hide_name = arg;

                } else if(previous.equals("--out")){
                    File file = new File(arg);
                    out_name = arg;
                } else {
                    System.out.println("Wow!");
                    usage();
                    return;
                }
                previous = null;
            }
        }
        if(previous != null || png_name == null){
            usage();
            return;
        }

        //System.out.println("Good...");
        Scanner sc = new Scanner(System.in);
        String password = null;
        byte [] key = null;
        if(raw == null){
            if(hide_name != null){
                System.out.println("Create a password: ");
            } else {
                System.out.println("Enter the password: ");
            }
            password = sc.nextLine();
            key = produce_key(password);
            password = null;
        }

        if(hide_name != null){
            // We're going to hide a file
            hide(png_name, hide_name, out_name, key);
        } else {
            // We're going to reveal a file
            reveal(png_name, out_name, key);
        }
        return;
    }
}
