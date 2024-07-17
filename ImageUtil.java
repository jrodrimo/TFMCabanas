package blockchain;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

public class ImageUtil {
    // Method to encode an image file to a Base64 string
    public static String encodeFileToBase64Binary(String path) throws Exception {
        // Read all bytes from the file at the specified path
        byte[] bytes = Files.readAllBytes(new File(path).toPath());
        // Encode the byte array into a Base64 string
        return Base64.getEncoder().encodeToString(bytes);
    }
}
