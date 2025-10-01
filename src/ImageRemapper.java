public interface ImageRemapper {

    /**
     * Read a single bit at a location in the image.
     */
    public boolean read(int location);

    /**
     * Write a single bit at a location in the image.
     */
    public void write(boolean bit, int location);

    /**
     * Get the true size of the image, which is the number of bits that can be
     * used to hide data.
     */
    public long getTrueSize();
}
