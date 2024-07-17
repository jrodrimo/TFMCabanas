package blockchain;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        // Base directory for images relative to the project location
        String baseDir = "../TFM/images/";
        // Directory for saving the results
        String resultDir = "../TFM/Results/";
        // Ensure that the results folder exists
        ensureDirectoryExists(resultDir);

        // Array with the names of the dataset folders
        String[] datasets = {"Dataset 1", "Dataset 2", "Dataset 3"};

        // Process each dataset
        for (String dataset : datasets) {
            System.out.println("Processing " + dataset);
            try {
                Blockchain myBlockchain = new Blockchain();
                File folder = new File(baseDir + dataset);
                File[] listOfFiles = folder.listFiles();  // List all files in the folder

                if (listOfFiles != null) {
                    for (File file : listOfFiles) {
                        if (file.isFile()) {
                            // Convert the image to Base64
                            String imageData = ImageUtil.encodeFileToBase64Binary(file.getAbsolutePath());
                            // Create a new block with the image information
                            Block newBlock = new Block(imageData, myBlockchain.getChain().
                            		get(myBlockchain.getChain().size() - 1).getBlockHash(), file.getName());
                            myBlockchain.addBlock(newBlock);
                        }
                    }
                }
                // Save the blockchain to a file in the results folder
                BlockchainUtil.saveBlockchainToFile(myBlockchain, resultDir + dataset.replace(" ", "_") + "_blockchain.txt");
            } catch (Exception e) {
                System.err.println("Error processing " + dataset + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Ensures that the specified directory exists and if not, creates it.
     * @param directoryPath The path of the directory to check and/or create.
     */
    private static void ensureDirectoryExists(String directoryPath) {
        File resultFolder = new File(directoryPath);
        if (!resultFolder.exists()) {
            resultFolder.mkdirs();  // Create the folder if it does not exist
        }
    }
}
