import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) {
        String routerIP = "127.0.0.1"; // IP address of ServerRouter
        int routerPort = 5555; // Port number ServerRouter listens on
        Socket routerSocket = null; // Socket to connect with ServerRouter
        PrintWriter out = null; // For sending to ServerRouter
        BufferedReader in = null; // For receiving from ServerRouter

        try {
            routerSocket = new Socket(routerIP, routerPort);
            out = new PrintWriter(routerSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(routerSocket.getInputStream()));
            System.out.println("Server connected to ServerRouter");

            // Communication loop with ServerRouter
            String fromClient;
            while ((fromClient = in.readLine()) != null) {
                System.out.println("Received from Client: " + fromClient);

                // Process data received from client
                String response = fromClient.toUpperCase(); // Conver to uppercase
                // Send the response back to ServerRouter
                System.out.println("Sent from Server: " + response);
                out.println(response);
            }
        } catch (IOException e) {
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