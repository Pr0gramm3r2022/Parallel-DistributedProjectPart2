import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) {
        String routerIP = "localhost"; // IP address of ServerRouter
        int routerPort = 5555; // Port number ServerRouter listens on
        Socket routerSocket = null; // Socket to connect with ServerRouter
        ObjectOutputStream out = null; // For sending objects to ServerRouter
        ObjectInputStream in = null; // For receiving objects from ServerRouter
        boolean running = true; // Loop flag
        boolean matrixTransmission = false; // Flag for matrix transmission
        matrix[] matrices = null; // Array of matrices to be received from ServerRouter
        int[][] resultMatrix = null; // Result of matrix multiplication


        try {
            routerSocket = new Socket(routerIP, routerPort);
            out = new ObjectOutputStream(routerSocket.getOutputStream());
            in = new ObjectInputStream(routerSocket.getInputStream());
            System.out.println("Server connected to ServerRouter");

            // Communication loop with ServerRouter
            Object fromClient;
            while (running) {
                // Check for start of matrix transmission
                fromClient = in.readObject();
                // Process data received from client
                String response = fromClient.toString().toUpperCase(); // Convert to uppercase
                System.out.println("Received from ServerRouter: " + fromClient);
                if (response.equals("START")) {
                    matrixTransmission = true;
                }
                if (matrixTransmission) {
                    if (fromClient instanceof matrix[]) {
                        matrices = (matrix[]) fromClient;
                        // Perform matrix multiplication
                        resultMatrix = MatrixFileIO.resultMatrix(matrices);
                        // Send response back to ServerRouter
                        out.writeObject(matrices);
                        out.flush();
                    }
                }

                // Send the response back to ServerRouter
                System.out.println("Sent from Server: " + response);
                out.writeObject(response);
                out.flush();

                // Check for termination
                if (response.equals("BYE.")) {
                    running = false;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error connecting to ServerRouter: " + e.getMessage());
        } finally {
            // Close connections
            System.out.println("TCPServer shutting down. Cleaning up connections.");

            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (routerSocket != null) routerSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}