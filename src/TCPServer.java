import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {
    private static class PerformanceMetrics {
        final long duration;
        final long baselineTime;
        final double speedup;
        final double efficiency;
        final int threadCount;
        final int matrixCount;
        final int matrixSize;

        PerformanceMetrics(long duration, long baselineTime, int threadCount,
                           int matrixCount, int matrixSize) {
            this.duration = duration;
            this.baselineTime = baselineTime;
            this.threadCount = threadCount;
            this.matrixCount = matrixCount;
            this.matrixSize = matrixSize;
            this.speedup = (double) baselineTime / duration;
            this.efficiency = speedup / threadCount;
        }

        void print() {
            System.out.println("\nPerformance Metrics:");
            System.out.println("Matrix Size: " + matrixSize + "x" + matrixSize);
            System.out.println("Number of matrices: " + matrixCount);
            System.out.println("Binary Tree Depth: " + (int) (Math.log(matrixCount) / Math.log(2)));
            System.out.println("Number of threads used: " + threadCount);
            System.out.println(String.format("Matrix multiplication time: %.4f seconds", duration / 1e9));
            System.out.println(String.format("Baseline time: %.4f seconds", baselineTime / 1e9));
            System.out.println(String.format("Speedup: %.2fx (%.2f%%)",
                    speedup, speedup * 100));
            System.out.println(String.format("Parallel Efficiency: %.2f (%.2f%%)",
                    efficiency, efficiency * 100));

            // Print thread allocation visualization
            printThreadAllocation(threadCount, matrixCount);
        }

        private void printThreadAllocation(int threads, int matrices) {
            System.out.println("\nThread Allocation in Binary Tree:");
            int levels = (int) (Math.log(matrices) / Math.log(2));
            int nodesInLastLevel = matrices / 2;

            // Build tree representation
            List<String> treeLines = new ArrayList<>();
            for (int level = 0; level <= levels; level++) {
                StringBuilder line = new StringBuilder();
                int nodesAtLevel = (int) Math.pow(2, level);
                int threadsPerNode = level == 0 ? 1 :
                        threads / (nodesAtLevel > threads ? threads : nodesAtLevel);

                // Add spacing for tree visualization
                int padding = (int) Math.pow(2, levels - level + 2);
                line.append(" ".repeat(padding));

                // Add nodes for this level
                for (int node = 0; node < nodesAtLevel &&
                        (level != levels || node < nodesInLastLevel); node++) {
                    if (level == levels) {
                        line.append("M").append(node * 2 + 1).append("-M").append(node * 2 + 2);
                    } else {
                        line.append(String.format("T%d", threadsPerNode));
                    }
                    line.append(" ".repeat(padding * 2));
                }
                treeLines.add(line.toString());
            }

            // Print the tree
            for (String line : treeLines) {
                System.out.println(line);
            }
        }
    }

    public static void main(String[] args) {
        String routerIP = "localhost";
        int routerPort = 5555;
        Socket routerSocket = null;
        ObjectOutputStream objectOut = null;
        ObjectInputStream objectIn = null;

        try {
            String serverAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server IP: " + serverAddress);

            routerSocket = new Socket(routerIP, routerPort);
            System.out.println("Connected to router socket");

            objectOut = new ObjectOutputStream(routerSocket.getOutputStream());
            objectOut.flush();
            objectIn = new ObjectInputStream(routerSocket.getInputStream());

            System.out.println("Sending SERVER identification...");
            objectOut.writeObject("SERVER");
            objectOut.flush();

            String confirmation = (String) objectIn.readObject();
            System.out.println("Router response: " + confirmation);

            boolean running = true;
            while (running) {
                try {
                    System.out.println("\nWaiting for incoming message...");
                    Object incoming = objectIn.readObject();
                    System.out.println("Received message of type: " +
                            (incoming != null ? incoming.getClass().getSimpleName() : "null"));

                    if (incoming instanceof String) {
                        String command = (String) incoming;
                        System.out.println("Received command: " + command);

                        if ("Start".equals(command)) {
                            processMatrices(objectIn, objectOut);
                        } else if ("Bye.".equals(command)) {
                            System.out.println("Received Bye command, ending session");
                            running = false;
                        }
                    }
                } catch (EOFException e) {
                    System.out.println("Client disconnected (EOF)");
                    break;
                } catch (SocketException e) {
                    System.out.println("Socket exception: " + e.getMessage());
                    break;
                } catch (StreamCorruptedException e) {
                    System.out.println("Stream corrupted: " + e.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cleanup(objectOut, objectIn, routerSocket);
        }
    }

    private static void processMatrices(ObjectInputStream objectIn, ObjectOutputStream objectOut)
            throws IOException, ClassNotFoundException, ExecutionException, InterruptedException {
        Object matricesObj = objectIn.readObject();

        if (matricesObj instanceof matrix[]) {
            matrix[] matrices = (matrix[]) matricesObj;
            int matrixSize = matrices[0].getRows();
            System.out.println("\nProcessing " + matrices.length +
                    " matrices of size " + matrixSize + "x" + matrixSize);

            // Initialize thread pool based on matrix count
            MatrixFileIO.initializeThreadPool(matrices.length);
            int threadCount = MatrixFileIO.getCurrentThreadCount();
            System.out.println("Initialized thread pool with " + threadCount + " threads");

            // Process matrices and measure time
            long startTime = System.nanoTime();
            int[][] result = MatrixFileIO.resultMatrix(matrices);
            long endTime = System.nanoTime();
            long duration = endTime - startTime;

            // Get baseline time
            long baselineTime = getBaselineTime(matrices);

            // Create and display metrics
            PerformanceMetrics metrics = new PerformanceMetrics(
                    duration, baselineTime, threadCount, matrices.length, matrixSize);
            metrics.print();

            // Send result back to client
            matrix finalMatrix = new matrix(result);
            objectOut.writeObject(finalMatrix);
            objectOut.flush();
        }
    }

    private static long getBaselineTime(matrix[] matrices)
            throws ExecutionException, InterruptedException {
        System.out.println("Calculating baseline (single-threaded) performance...");

        MatrixFileIO.initializeThreadPool(2); // Forces single thread mode
        long startTime = System.nanoTime();
        MatrixFileIO.resultMatrix(matrices.clone()); // Use clone to avoid modifying original
        long endTime = System.nanoTime();

        return endTime - startTime;
    }

    private static void cleanup(ObjectOutputStream objectOut,
                                ObjectInputStream objectIn, Socket routerSocket) {
        System.out.println("\nCleaning up server resources...");
        try {
            MatrixFileIO.shutdown();

            if (objectOut != null) objectOut.close();
            if (objectIn != null) objectIn.close();
            if (routerSocket != null) routerSocket.close();

            System.out.println("Cleanup complete");
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}