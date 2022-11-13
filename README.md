# Occult
PNG steganography/cryptography program

This program allows you to encrypt and hide files in PNG files by storing them in the lowest order bits of color. The program expects a PNG file name as input. If you are hiding a file, use --hide followed by the file to hide and optionally --out followed by the output PNG file. If you are revealing a file, you do not need to add another parameter but optionally --out to specify the output file. The process can take several seconds especially on large PNGs. The program expects you to create a password when encrypting and to enter the password when decrypting.

The most recent addition I made was to improve the way in which files are hidden so that it should be much harder to determine whether a particular PNG is hiding a file or not. One major limitation is that hiding a larger file tends to increase the file size of the PNG which could cause suspicion.

Future features to add to this program include AES256 and allowing the user to choose between more secure but slower mappings versus less secure but fast mappings.
