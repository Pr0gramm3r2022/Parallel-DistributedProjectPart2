import java.io.*;
import java.net.*;
import java.util.*;

public class TCPClient {
    public static void main(String[] args) {
        String routerIP = "192.168.50.119";
        int routerPort = 5555;
        Socket socket = null;
        ObjectOutputStream objectOut = null;
        ObjectInputStream objectIn = null;

        try {
            // Connect to router
            socket = new Socket(routerIP, routerPort);
            System.out.println("Connected to router at " + routerIP + ":" + routerPort);

            // Get local address
            String localAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Client IP: " + localAddress);

            // Create object output stream first and flush
            objectOut = new ObjectOutputStream(socket.getOutputStream());
            objectOut.flush();

            // Then create input stream
            objectIn = new ObjectInputStream(socket.getInputStream());

            // Send destination address (localhost for server)
            objectOut.writeObject("127.0.0.1");
            objectOut.flush();

            // Get confirmation
            Object response = objectIn.readObject();
            System.out.println("Router response: " + response);

            // Get user input
            Scanner scanner = new Scanner(System.in);
            System.out.print("Size of Matrices: ");
            int matrixSize = scanner.nextInt();
            System.out.print("Number of Matrices: ");
            int numMatrices = scanner.nextInt();

            System.out.println("Generating " + numMatrices + " matrices of size " + matrixSize + "x" + matrixSize);

            // Generate matrices
            matrix[] matrices = new matrix[numMatrices];
            for (int i = 0; i < numMatrices; i++) {
                matrices[i] = new matrix(MatrixGenerator.generateMatrix(matrixSize, 1, 10)); // Using smaller numbers for readability
                System.out.println("Generated matrix " + (i + 1) + ":");
                printMatrix(matrices[i].getMatrixData(), 100); // Print first 5x5 of each matrix
            }

            // Send start signal
            System.out.println("\nSending start signal");
            objectOut.writeObject("Start");
            objectOut.flush();

            // Send matrices
            System.out.println("Sending matrices");
            objectOut.writeObject(matrices);
            objectOut.flush();

            // Send end signal
            System.out.println("Sending end signal");
            objectOut.writeObject("End");
            objectOut.flush();

            // Wait for result
            System.out.println("Waiting for result...");
            Object result = objectIn.readObject();

            if (result instanceof matrix) {
                System.out.println("\nReceived result matrix:");
                int[][] resultData = ((matrix) result).getMatrixData();
                printMatrix(resultData, 100);

            } else {
                System.out.println("Received unexpected result type: " +
                        (result != null ? result.getClass().getSimpleName() : "null"));
            }

            // Send goodbye
            System.out.println("\nSending goodbye");
            objectOut.writeObject("Bye.");
            objectOut.flush();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Closing connections...");
            try {
                if (objectOut != null) objectOut.close();
                if (objectIn != null) objectIn.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    // Helper method to print a matrix
    private static void printMatrix(int[][] matrix, int maxSize) {
        int size = Math.min(maxSize, matrix.length);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.printf("%4d ", matrix[i][j]);
            }
            System.out.println();
        }
        if (matrix.length > maxSize) {
            System.out.println("... (matrix continues)");
        }
    }
}