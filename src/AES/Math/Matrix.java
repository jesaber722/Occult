package AES.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Matrix<A> {

    private int rows, cols;

    private ArrayList<ArrayList<Field<A>>> values;

    public Matrix(List<List<Field<A>>> vals) {
        rows = vals.size();
        cols = vals.get(0).size();

        values = new ArrayList<>();

        for (List<Field<A>> r : vals) {
            ArrayList<Field<A>> row = new ArrayList<>(r);
            values.add(row);
        }
    }

    public Matrix(String str) {
        str = str.trim();
        int bracketCount = 0;
        Pattern rowPattern = Pattern.compile("\\[[\\d\\s]*\\]");
        Matcher matcher = rowPattern.matcher(str);
        System.out.println(matcher.lookingAt());
        System.out.println("ok");
    }

    public static void main(String [] args){
        new Matrix<GF256>("[3456 6456 6 3]");
    }
}
