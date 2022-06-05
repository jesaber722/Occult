public interface ImageRemapper {
    public boolean read(int location);

    public void write(boolean bit, int location);

    public long getTrueSize();
}
