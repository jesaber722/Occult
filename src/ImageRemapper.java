
/**
 * image remappers read/write a bit at a certain location in the image. This location is
 * determined by the remapping being used. Ideally, it should hide things in a "random" fashion,
 * so it is hard to tell if a png contains a hidden message.
 */
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
