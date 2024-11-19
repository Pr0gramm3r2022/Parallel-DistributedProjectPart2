import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) {
        String routerIP = "127.0.0.1"; // IP address of ServerRouter
        int routerPort = 5555; // Port number ServerRouter listens on
        Socket routerSocket = null; // Socket to connect with ServerRouter
        ObjectOutputStream out = null; // For sending objects to ServerRouter
        ObjectInputStream in = null; // For receiving objects from ServerRouter

        try {
            routerSocket = new Socket(routerIP, routerPort);
            out = new ObjectOutputStream(routerSocket.getOutputStream());
            in = new ObjectInputStream(routerSocket.getInputStream());
            System.out.println("Server connected to ServerRouter");

            // Communication loop with ServerRouter
            Object fromClient;
            while ((fromClient = in.readObject()) != null) {
                System.out.println("Received from Client: " + fromClient);

                // Check if the received object is a 2D array
                if (fromClient instanceof int[][]) {
                    int[][] receivedArray = (int[][]) fromClient;
                    System.out.println("Received 2D array:");
                    for (int[] row : receivedArray) {
                        for (int cell : row) {
                            System.out.print(cell + " ");
                        }
                        System.out.println();
                    }

                    // Send back the same array as a response (or modify if needed)
                    out.writeObject(receivedArray);
                    out.flush();
                } else {
                    System.out.println("Received unsupported object type.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error connecting to ServerRouter: " + e.getMessage());
        } finally {
            // Close connections
            System.out.println("TCPServer shutting down. Cleaning up connections.");

            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
                if (routerSocket != null)
                    routerSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}
