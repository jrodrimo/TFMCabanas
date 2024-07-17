package blockchain;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    // A list to store the chain of blocks
    private List<Block> chain;

    // Constructor that initializes the blockchain with a genesis block
    public Blockchain() {
        chain = new ArrayList<>();  // Initialize the ArrayList to hold the chain
        // Add the genesis block to the blockchain with a previous hash of "0"
        chain.add(new Block("Genesis Block", "0", "Genesis"));
    }

    // Method to add a new block to the blockchain
    public void addBlock(Block newBlock) {
        chain.add(newBlock);  // Append the new block to the end of the chain
    }

    // Getter method to retrieve the entire blockchain
    public List<Block> getChain() {
        return chain;  // Return the list representing the chain
    }
}
