import java.io.*;
import java.net.*;

public class TCPClient {
    public static void main(String[] args) throws IOException {
        String routerIP = "129.80.181.47"; // IP of ServerRouter
        int routerPort = 5555; // Port ServerRouter listens on
        String localAddress = InetAddress.getLocalHost().getHostAddress();
        Socket socket = null; // Socket to connect to ServerRouter
        PrintWriter out = null; // For sending to ServerRouter
        BufferedReader in = null; // For receiving from ServerRouter
        BufferedReader fromFile = null; // File reader

        try {
            System.out.println("Client IP: " + localAddress);

            // Connect to ServerRouter
            socket = new Socket(routerIP, routerPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to ServerRouter at " + routerIP + ":" + routerPort);

            // Communication loop
            fromFile = new BufferedReader(new FileReader("file.txt"));
            String fileLine, serverResponse;
            while ((fileLine = fromFile.readLine()) != null) {
                System.out.println("Sent from Client: " + fileLine);

                out.println(fileLine); // Send message to ServerRouter

                // Read response from server
                if ((serverResponse = in.readLine()) != null) {
                    System.out.println("Response from Server: " + serverResponse);
                    if (serverResponse.equalsIgnoreCase("Bye.")) break; // Exit statement
                } else {
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about the ServerRouter: " + routerIP);
        } catch (IOException e) {
            System.err.println("Couldn't connect to ServerRouter: " + e.getMessage());
        } finally {
            // Close all connections
            System.out.println("TCPClient shutting down. Cleaning up connections.");

            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
                if (fromFile != null) fromFile.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}
