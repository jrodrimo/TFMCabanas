package blockchain;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class BlockchainUtil {
    /**
     * Saves the blockchain data to a file.
     * @param blockchain The blockchain instance containing all the blocks.
     * @param filename The path to the file where the blockchain should be saved.
     */
    public static void saveBlockchainToFile(Blockchain blockchain, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            // Iterate through each block in the blockchain
            for (Block block : blockchain.getChain()) {
                // Write the image name of the block to the file
                writer.write("Image Name: " + block.getImageName() + "\n");
                // Write the hash of the block to the file
                writer.write("Block Hash: " + block.getBlockHash() + "\n");
                // Write the hash of the previous block to the file
                writer.write("Previous Hash: " + block.getPreviousHash() + "\n");
                // Write a separator to visually separate each block in the file
                writer.write("---------------------------------\n");
            }
        } catch (IOException e) {
            // Print an error message if there is an issue writing to the file
            System.err.println("Error writing the blockchain to the file: " + e.getMessage());
        }
    }
}
