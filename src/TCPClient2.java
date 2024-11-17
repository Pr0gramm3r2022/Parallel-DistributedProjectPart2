
import java.io.*;
import java.net.*;

public class TCPClient2 {
    public static void main(String[] args) throws IOException {
        String routerIP = "129.80.181.47"; // IP of ServerRouter
        int routerPort = 5555; // Port ServerRouter listens on
        String localAddress = InetAddress.getLocalHost().getHostAddress();
        Socket socket = null; // Socket to connect to ServerRouter
        PrintWriter out = null; // For sending to ServerRouter
        BufferedReader in = null; // For receiving from ServerRouter
        BufferedReader fromFile = null; // File reader

        try {
            System.out.println("Client2 IP: " + localAddress);

            // Connect to ServerRouter
            socket = new Socket(routerIP, routerPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to ServerRouter at " + routerIP + ":" + routerPort);

            // Communication loop with file input first
            fromFile = new BufferedReader(new FileReader("file.txt"));
            String fileLine, serverResponse;
            while ((fileLine = fromFile.readLine()) != null) {
                System.out.println("Sent from Client2: " + fileLine);

                out.println(fileLine); // Send message to ServerRouter

                // Read response from server
                if ((serverResponse = in.readLine()) != null) {
                    System.out.println("Response from Server: " + serverResponse);
                } else {
                    System.out.println("Server closed the connection.");
                    break;
                }
            }

            // Now allow manual input after file completion
            System.out.println("Enter messages to send to the server. Type 'Bye.' to exit:");
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String userLine;
            while (true) {
                System.out.print("Enter message: ");
                userLine = userInput.readLine(); // Read user input

                if (userLine == null || userLine.equalsIgnoreCase("Bye.")) {
                    out.println("Bye."); // Optionally notify the server about disconnection
                    break;
                }

                out.println(userLine); // Send user input to ServerRouter
                if ((serverResponse = in.readLine()) != null) {
                    System.out.println("Response from Server: " + serverResponse);
                } else {
                    System.out.println("Server closed the connection.");
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about the ServerRouter: " + routerIP);
        } catch (IOException e) {
            System.err.println("Couldn't connect to ServerRouter: " + e.getMessage());
        } finally {
            // Close all connections
            System.out.println("TCPClient2 shutting down. Cleaning up connections.");

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
