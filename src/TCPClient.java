import java.io.*;
import java.net.*;
import java.util.*;

public class TCPClient {
    public static void main(String[] args) throws IOException {
        String routerIP = "129.80.181.47"; // IP of ServerRouter
        int routerPort = 5555; // Port ServerRouter listens on
        String localAddress = InetAddress.getLocalHost().getHostAddress();
        Socket socket = null; // Socket to connect to ServerRouter
        PrintWriter out = null; // For sending to ServerRouter
        BufferedReader in = null; // For receiving from ServerRouter
        boolean running = true; // Loop flag

        // Get user input for matrix size and number of matrices
        Scanner scanner = new Scanner(System.in);
        System.out.print("Size of Matrices: ");
        int matrixSize = scanner.nextInt();
        System.out.print("Number of Matrices: ");
        int numMatrices = scanner.nextInt();

        // Generate matrices
        matrix[] matrices = new matrix[numMatrices];
        for (int i = 0; i < numMatrices; i++) {
            int[][] newMatrix = MatrixGenerator.generateMatrix(matrixSize);

            matrices[i] = new matrix(newMatrix);
        }

        try {
            // Get local IP
            System.out.println("Client IP: " + localAddress);

            // Connect to ServerRouter
            socket = new Socket(routerIP, routerPort);
            ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectIn = new ObjectInputStream(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to ServerRouter at " + routerIP + ":" + routerPort);

            // Communication loop
            String serverResponse;
            while (running) {
                // Send matrices to ServerRouter
                objectOut.writeObject(matrices);

                // Send end of matrix transmission
                objectOut.writeObject("End");

                // Get response Matrix from ServerRouter
                matrix[] responseMatrices = (matrix[]) objectIn.readObject();

                // Read response from server
                if ((serverResponse = in.readLine()) != null) {
                    System.out.println("Response from Server: " + serverResponse);
                    if (serverResponse.equalsIgnoreCase("Bye.")) {
                        running = false;
                    }
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about the ServerRouter: " + routerIP);
        } catch (IOException e) {
            System.err.println("Couldn't connect to ServerRouter: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            // Close all connections
            System.out.println("TCPClient shutting down. Cleaning up connections.");

            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}
