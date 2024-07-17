package blockchain;

import java.security.MessageDigest;
import java.util.Date;

public class Block {
    // Private variables to store the previous hash, the hash of this block, image data, image name, and timestamp
    private String previousHash;
    private String blockHash;
    private String imageData;
    private String imageName;  // Image name for identification
    private long timeStamp;

    // Constructor to initialize a block with image data, the previous block's hash, and the image name
    public Block(String imageData, String previousHash, String imageName) {
        this.imageData = imageData; // Store the image data as a string
        this.previousHash = previousHash; // Store the hash of the previous block
        this.imageName = imageName; // Store the name of the image for easier identification
        this.timeStamp = new Date().getTime(); // Get the current timestamp when the block is created
        this.blockHash = calculateHash(); // Generate the hash for this block
    }

    // Method to calculate the hash of the block using SHA-256 algorithm
    public String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); // Get an instance of SHA-256
            String dataToHash = previousHash + Long.toString(timeStamp) + imageData; // Concatenate previous hash, timestamp, and image data to form the data string to hash
            byte[] hash = digest.digest(dataToHash.getBytes("UTF-8")); // Compute the hash
            StringBuilder hexString = new StringBuilder(); // StringBuilder to convert the byte array to hex format
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b); // Convert each byte to hex
                if (hex.length() == 1) hexString.append('0'); // Pad with leading zero if necessary
                hexString.append(hex); // Append the hex value to the string builder
            }
            return hexString.toString(); // Convert the StringBuilder to String and return the hash
        } catch(Exception e) {
            throw new RuntimeException(e); // Throw a runtime exception if hashing fails
        }
    }

    // Getter methods to access block details
    public String getBlockHash() {
        return blockHash; // Return the hash of this block
    }

    public String getPreviousHash() {
        return previousHash; // Return the hash of the previous block
    }

    public String getImageName() {
        return imageName; // Return the name of the image
    }
}
