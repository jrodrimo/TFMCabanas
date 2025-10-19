package blockchain;

import io.ipfs.multiaddr.MultiAddress;
import ipfs.api.IPFS;
import ipfs.api.MerkleNode;
import ipfs.api.NamedStreamable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

/**
 * Extends your Main:
 *  - Uploads each original image file to IPFS and times it
 *  - Uploads each block (JSON bytes) to IPFS and times it
 *  - Uploads the final saved blockchain file to IPFS and times it
 */
public class Main {

    public static void main(String[] args) {
        // Base directory for images
        String baseDir = "C:\\Users\\jesus.rodriguezm\\Documents\\Paperback Writer\\TFM Cabañas\\TFM\\TFM\\images\\";
        // Directory for saving the results
        String resultDir = "C:\\Users\\jesus.rodriguezm\\Documents\\Paperback Writer\\TFM Cabañas\\TFM\\TFM\\Results\\";

        ensureDirectoryExists(resultDir);

        // CSVs for your local timings (already existed)
        File base64Csv = new File(resultDir + "base64_times.csv");
        File blockCsv  = new File(resultDir + "block_times.csv");

        // NEW CSVs for IPFS uploads
        File ipfsImageCsv      = new File(resultDir + "ipfs_image_add_times.csv");
        File ipfsBlockCsv      = new File(resultDir + "ipfs_block_add_times.csv");
        File ipfsChainFileCsv  = new File(resultDir + "ipfs_blockchain_file_add_times.csv");

        // Prepare headers (overwrite each run)
        try (PrintWriter pw1 = new PrintWriter(new FileWriter(base64Csv, false));
             PrintWriter pw2 = new PrintWriter(new FileWriter(blockCsv,  false));
             PrintWriter pw3 = new PrintWriter(new FileWriter(ipfsImageCsv, false));
             PrintWriter pw4 = new PrintWriter(new FileWriter(ipfsBlockCsv, false));
             PrintWriter pw5 = new PrintWriter(new FileWriter(ipfsChainFileCsv, false))) {

            pw1.println("dataset,filename,encode_ms");
            pw2.println("dataset,filename,add_block_ms");
            pw3.println("dataset,filename,bytes,add_ms,cid");
            pw4.println("dataset,filename,block_index,bytes,add_ms,cid");
            pw5.println("dataset,blockchain_filename,bytes,add_ms,cid");

        } catch (IOException e) {
            System.err.println("Error preparing CSV files: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Connect to local IPFS daemon
        IPFS ipfs = new IPFS(new MultiAddress("/ip4/127.0.0.1/tcp/5001"));

        String imageData = "";
        String[] datasets = {"Dataset 1", "Dataset 2", "Dataset 3"};

        for (String dataset : datasets) {
            System.out.println("Processing " + dataset);
            try {
                Blockchain myBlockchain = new Blockchain();
                File folder = new File(baseDir + dataset);
                File[] listOfFiles = folder.listFiles();

                if (listOfFiles != null) {
                    Arrays.sort(listOfFiles, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

                    int blockIndex = 0;
                    for (File file : listOfFiles) {
                        if (!file.isFile()) continue;

                        String fileName = file.getName();

                        // ---- 1) Measure Base64 encoding time (your existing metric) ----
                        long t0 = System.nanoTime();
                        imageData = ImageUtil.encodeFileToBase64Binary(file.getAbsolutePath());
                        long t1 = System.nanoTime();
                        double encodeMs = (t1 - t0) / 1_000_000.0;

                        // ---- 2) Create and add block, measure add time (your existing metric) ----
                        Block newBlock = new Block(
                                imageData,
                                myBlockchain.getChain().get(myBlockchain.getChain().size() - 1).getBlockHash(),
                                fileName
                        );
                        long t2 = System.nanoTime();
                        myBlockchain.addBlock(newBlock);
                        long t3 = System.nanoTime();
                        double addBlockMs = (t3 - t2) / 1_000_000.0;

                        appendCsvLine(base64Csv, dataset + "," + escapeCsv(fileName) + "," + encodeMs);
                        appendCsvLine(blockCsv,  dataset + "," + escapeCsv(fileName) + "," + addBlockMs);

                        // ---- 3) Upload ORIGINAL IMAGE BYTES to IPFS and time it ----
                        byte[] imageBytes = Files.readAllBytes(file.toPath());
                        NamedStreamable.ByteArrayWrapper imgNs =
                                new NamedStreamable.ByteArrayWrapper(fileName, imageBytes);

                        long ti0 = System.nanoTime();
                        List<MerkleNode> imgNodes = ipfs.add(imgNs);
                        long ti1 = System.nanoTime();
                        double imgAddMs = (ti1 - ti0) / 1_000_000.0;

                        String imgCid = (imgNodes != null && !imgNodes.isEmpty())
                                ? imgNodes.get(0).hash.toString()
                                : "";
                        appendCsvLine(ipfsImageCsv,
                                dataset + "," + escapeCsv(fileName) + "," + imageBytes.length + "," + imgAddMs + "," + imgCid);

                        // ---- 4) Upload BLOCK (as JSON bytes) to IPFS and time it ----
                        // If you have a JSON serializer already, use it; else quick minimal JSON
                        // Example: serialize fields you care about:
                        String blockJson = BlockJsonSerializer.toJson(newBlock); // implement below
                        byte[] blockBytes = blockJson.getBytes("UTF-8");
                        String blockAssetName = fileName + ".block.json";

                        NamedStreamable.ByteArrayWrapper blockNs =
                                new NamedStreamable.ByteArrayWrapper(blockAssetName, blockBytes);

                        long tb0 = System.nanoTime();
                        List<MerkleNode> blockNodes = ipfs.add(blockNs);
                        long tb1 = System.nanoTime();
                        double blockAddMs = (tb1 - tb0) / 1_000_000.0;

                        String blockCid = (blockNodes != null && !blockNodes.isEmpty())
                                ? blockNodes.get(0).hash.toString()
                                : "";
                        appendCsvLine(ipfsBlockCsv,
                                dataset + "," + escapeCsv(fileName) + "," + blockIndex + "," + blockBytes.length + "," + blockAddMs + "," + blockCid);

                        blockIndex++;
                    }
                }

                // ---- 5) Save the blockchain file (your existing call) ----
                String outPath = resultDir + dataset.replace(" ", "_") + "_blockchain.txt";
                BlockchainUtil.saveBlockchainToFile64(imageData, myBlockchain, outPath);

                // ---- 6) Upload the blockchain file itself to IPFS and time it ----
                File chainFile = new File(outPath);
                byte[] chainBytes = Files.readAllBytes(chainFile.toPath());
                NamedStreamable.ByteArrayWrapper chainNs =
                        new NamedStreamable.ByteArrayWrapper(chainFile.getName(), chainBytes);

                long tc0 = System.nanoTime();
                List<MerkleNode> chainNodes = ipfs.add(chainNs);
                long tc1 = System.nanoTime();
                double chainAddMs = (tc1 - tc0) / 1_000_000.0;

                String chainCid = (chainNodes != null && !chainNodes.isEmpty())
                        ? chainNodes.get(0).hash.toString()
                        : "";
                appendCsvLine(ipfsChainFileCsv,
                        dataset + "," + escapeCsv(chainFile.getName()) + "," + chainBytes.length + "," + chainAddMs + "," + chainCid);

            } catch (Exception e) {
                System.err.println("Error processing " + dataset + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Done. CSVs written to: " + base64Csv.getAbsolutePath() + " and " + blockCsv.getAbsolutePath());
        System.out.println("IPFS CSVs written to: " + ipfsImageCsv.getAbsolutePath()
                + ", " + ipfsBlockCsv.getAbsolutePath()
                + ", " + ipfsChainFileCsv.getAbsolutePath());
    }

    private static void ensureDirectoryExists(String directoryPath) {
        File resultFolder = new File(directoryPath);
        if (!resultFolder.exists()) resultFolder.mkdirs();
    }

    private static void appendCsvLine(File csv, String line) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(csv, true))) {
            pw.println(line);
        } catch (IOException e) {
            System.err.println("Error writing CSV line to " + csv.getName() + ": " + e.getMessage());
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        String v = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + v + "\"" : v;
    }

    /**
     * Minimal JSON serializer for a Block.
     * Replace with your preferred JSON lib (e.g., Gson/Jackson) if available.
     */
    static class BlockJsonSerializer {
        static String toJson(Block b) {
            // Adjust according to your Block fields / getters
            // Assuming: data (Base64 string), previousHash, fileName, blockHash, timestamp, nonce, etc.
            String data = safe(b.getData());
            String prev = safe(b.getPreviousHash());
            String name = safe(b.getFileName());
            String hash = safe(b.getBlockHash());
            long ts = 0L;
            try { ts = b.getTimestamp(); } catch (Throwable ignored) {}

            return "{"
                    + "\"fileName\":\"" + name + "\","
                    + "\"previousHash\":\"" + prev + "\","
                    + "\"blockHash\":\"" + hash + "\","
                    + "\"timestamp\":" + ts + ","
                    + "\"data\":\"" + data + "\""
                    + "}";
        }
        private static String safe(String s) {
            if (s == null) return "";
            // escape quotes/backslashes minimally for JSON
            return s.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }
}
