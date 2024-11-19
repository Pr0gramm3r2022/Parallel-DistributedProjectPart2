import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) {
        String routerIP = "129.80.181.47"; // IP of ServerRouter
        int routerPort = 5555; // Port ServerRouter listens on
        Socket socket = null; // Socket to connect to ServerRouter
        ObjectOutputStream out = null; // For sending objects to ServerRouter
        ObjectInputStream in = null; // For receiving objects from ServerRouter

        try {
            // Connect to ServerRouter
            socket = new Socket(routerIP, routerPort);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Connected to ServerRouter at " + routerIP + ":" + routerPort);

            // Example 2D array to send
            int[][] arrayToSend = {
                    { 1, 2, 3 },
                    { 4, 5, 6 },
                    { 7, 8, 9 }
            };

            // Send the 2D array to the server
            System.out.println("Sending 2D array to the server...");
            out.writeObject(arrayToSend);
            out.flush();

            // Receive the response (processed 2D array) from the server
            Object response = in.readObject();
            if (response instanceof int[][]) {
                int[][] receivedArray = (int[][]) response;
                System.out.println("Received 2D array from the server:");
                for (int[] row : receivedArray) {
                    for (int cell : row) {
                        System.out.print(cell + " ");
                    }
                    System.out.println();
                }
            } else {
                System.out.println("Unexpected response from server: " + response);
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about the ServerRouter: " + routerIP);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Couldn't connect to ServerRouter or handle response: " + e.getMessage());
        } finally {
            // Close all connections
            System.out.println("TCPClient shutting down. Cleaning up connections.");

            try {
                if (out != null)
                    out.close();
                if (in != null)
                    in.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}
