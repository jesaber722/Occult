import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class Occult {


    private static void usage(){
        System.out.println("occult picture.png [--hide file] [--out file] [--raw]");
    }

    public static int main(String [] args) {
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
                        return 1;
                    }
                    raw = "y";
                } else if(arg.equals("--hide")){
                    if(hide_name != null){
                        usage();
                        return 1;
                    }
                    previous = arg;
                } else if(arg.equals("--out")){
                    if(out_name != null){
                        usage();
                        return 1;
                    }
                    previous = arg;
                } else if(arg.charAt(0) == '-'){
                    usage();
                    return 1;
                } else {
                    if(png_name != null){
                        usage();
                        return 1;
                    }
                    png_name = arg;
                }
            } else {
                if(previous.equals("--hide")){
                    try{
                        Paths.get(arg);
                    } catch (InvalidPathException e){
                        System.out.println("File to hide not found");
                        return 1;
                    }
                    hide_name = arg;

                } else if(previous.equals("--out")){
                    try{
                        Paths.get(arg);
                    } catch(InvalidPathException e){
                        System.out.println("Output file not found");
                        return 1;
                    }
                    out_name = arg;
                } else {
                    System.out.println("Wow!");
                    usage();
                    return 1;
                }
                previous = null;
            }
        }
        if(previous != null){
            usage();
            return 1;
        }
        System.out.println("Good...");
        return 0;
    }
}
