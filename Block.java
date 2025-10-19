package blockchain;

import java.security.MessageDigest;
import java.util.Date;
import java.nio.charset.StandardCharsets;

/**
 * Represents one block in the blockchain.
 * Each block contains:
 *  - the hash of the previous block
 *  - its own computed hash
 *  - the Base64-encoded image data
 *  - the image file name
 *  - the timestamp of creation
 */
public class Block {

    private final String previousHash;
    private final String blockHash;
    private final String imageData;
    private final String imageName;
    private final long timeStamp;

    /**
     * Creates a new block containing the image data, previous hash, and image name.
     */
    public Block(String imageData, String previousHash, String imageName) {
        this.imageData = imageData;
        this.previousHash = previousHash;
        this.imageName = imageName;
        this.timeStamp = new Date().getTime(); // current system time
        this.blockHash = calculateHash(); // compute block hash once upon creation
    }

    /**
     * Calculates this block’s hash using SHA-256 of previousHash + timestamp + imageData.
     */
    private String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String dataToHash = previousHash + timeStamp + imageData;
            byte[] hash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error calculating hash", e);
        }
    }

    // ---------------------------
    // Getters
    // ---------------------------

    public String getBlockHash() {
        return blockHash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getData() {
        return imageData;
    }

    public String getFileName() {
        return imageName;
    }

    public long getTimestamp() {
        return timeStamp;
    }

    // ---------------------------
    // Optional helper
    // ---------------------------

    /**
     * Returns a minimal JSON representation of this block (for easy debugging).
     * Note: this is separate from the BlockJsonSerializer used in Main.java.
     */
    public String toJson() {
        return "{"
                + "\"imageName\":\"" + escape(imageName) + "\","
                + "\"previousHash\":\"" + escape(previousHash) + "\","
                + "\"blockHash\":\"" + escape(blockHash) + "\","
                + "\"timestamp\":" + timeStamp + ","
                + "\"imageData\":\"" + escape(imageData) + "\""
                + "}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
