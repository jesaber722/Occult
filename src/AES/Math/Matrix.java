package AES.Math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Matrix<A extends Field<A>> {

    private Supplier<A> ctor;

    private int rows, cols;

    private ArrayList<ArrayList<Field<A>>> values;
    public Matrix(List<List<Field<A>>> vals) {

        rows = vals.size();
        cols = vals.get(0).size();

        for(List<Field<A>> r: vals){
            if(r.size() != cols){
                throw new IllegalArgumentException("Invalid matrix");
            }
        }
        values = new ArrayList<>();
        for (List<Field<A>> r : vals) {
            ArrayList<Field<A>> row = new ArrayList<>(r);
            values.add(row);
        }
    }


    /**
     * I just barely squeezed a way to do what I want to do here. All of these complications could be avoided
     * if java statics were specific to the version of the generic class, but oh well.
     * There is a caveat that you must provide a "Supplier".
     * @param ctr
     * @param str
     */
    public Matrix(Supplier<A> ctr, String str) {
        this.ctor = ctr;
        ArrayList<ArrayList<Field<A>>> matrixParam = new ArrayList<>();
        str = str.trim();
        int bracketCount = 0;
        Pattern rowPattern = Pattern.compile("\\[[a-zA-Z\\d\\s]*\\]");
        Matcher matcher = rowPattern.matcher(str);
        /*
        System.out.println(matcher.lookingAt());
        System.out.println(matcher.matches());
        matcher.find();
        System.out.println(matcher.group());
        System.out.println("ok");
         */
        int rowLength = -1;
        while(matcher.find()){
            ArrayList<Field<A>> rowParam = new ArrayList<>();
            String row_str = matcher.group();
            System.out.println(row_str);
            if(row_str.length() == 2) {
                throw new IllegalArgumentException("Invalid Matrix String");
            }
            row_str = row_str.substring(1, row_str.length() - 1);
            System.out.println(row_str);
            String [] row_input = row_str.split(" ");
            if(rowLength == -1){
                rowLength = row_input.length;
            } else if(rowLength != row_input.length){
                throw new IllegalArgumentException("Invalid Matrix String");
            }
            for(int i = 0; i < row_input.length; i++){
                rowParam.add(ctor.get().constructFromString(row_input[i]));
            }
            matrixParam.add(rowParam);
        }

        rows = matrixParam.size();
        cols = matrixParam.get(0).size();

        for(List<Field<A>> r: matrixParam){
            if(r.size() != cols){
                throw new IllegalArgumentException("Invalid matrix");
            }
        }
        values = new ArrayList<>();
        for (List<Field<A>> r : matrixParam) {
            ArrayList<Field<A>> row = new ArrayList<>(r);
            values.add(row);
        }
    }

    public int getNumRows(){
        return rows;
    }

    public int getNumCols(){
        return cols;
    }

    public String getDimensionString(){
        return rows + "x" + cols;
    }

    public Field<A> get(int r, int c){
        return values.get(r).get(c);
    }

    public Matrix<A> add(Matrix<A> other){
        if(rows != other.getNumRows() || cols != other.getNumCols()){
            throw new IllegalArgumentException("Cannot add matrices of size " + getDimensionString() +
                    " and " + other.getDimensionString());
        }
        List<List<Field<A>>> vals = new ArrayList<>();
        for(int i = 0; i < rows; i++){
            List<Field<A>> val_row = new ArrayList<>();
            for(int j = 0; j < cols; j++){
                val_row.add(this.get(i, j).add(other.get(i, j).itself()));
            }
            vals.add(val_row);
        }
        return new Matrix<>(vals);
    }

    public Matrix<A> multiply(Matrix<A> other){
        if(cols != other.getNumRows()){
            throw new IllegalArgumentException("Cannot multiply matrices of size " + getDimensionString() +
                    " and " + other.getDimensionString());
        }
        List<List<Field<A>>> vals = new ArrayList<>();
        for(int i = 0; i < rows; i++){
            List<Field<A>> val_row = new ArrayList<>();
            for(int j = 0; j < other.cols; j++){
                Field<A> val = ctor.get(); // just to initialize so Java won't complain
                for(int k = 0; k < this.cols; k++){
                    if(k == 0){
                        val = this.get(i, k).multiply(other.get(k, j).itself());
                    } else {
                        val = val.add(this.get(i, k).multiply(other.get(k, j).itself()));
                    }
                }
                val_row.add(val);
            }
            vals.add(val_row);
        }
        return new Matrix<>(vals);
    }

    public String toString(){
        StringBuilder result = new StringBuilder();
        result.append("[");
        for(int i = 0; i < rows; i++){
            result.append("[");
            for(int j = 0; j < cols; j++){
                result.append("[");
                result.append(get(i, j).toString());
                if(j < cols - 1)
                    result.append("|");
                result.append("]");
            }
            result.append("]");
        }
        return result.toString();
    }

    public static void main(String [] args){
        System.out.println(new GF256("23"));
        Matrix<GF256> M = new Matrix<GF256>(GF256.getSupplier(), "[34 56 6456][15 23 44]");
        System.out.println(M.toString());
        System.out.println();
        System.out.println(new GF256("53"));
        System.out.println(new GF256("2"));
        System.out.println(new GF256("88"));
        System.out.println(new GF256("221"));
        System.out.println(new GF256("42"));
        System.out.println(new GF256("66"));
        System.out.println(new GF256("145"));
        System.out.println(new GF256("241"));
        Matrix<GF256> M1 = new Matrix<GF256>(GF256.getSupplier(), "[53 2 88 221][0 0 0 0]");
        Matrix<GF256> M2 = new Matrix<GF256>(GF256.getSupplier(), "[42 0][66 0][145 0][241 0]");
    }
}
