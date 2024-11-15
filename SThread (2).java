import java.io.*;
import java.net.*;
import java.util.Hashtable;

public class SThread extends Thread {
    private Hashtable<String, Socket> routingTable; // The routing table
    private PrintWriter outToClient, outToServer; // For sending to client and server
    private BufferedReader inFromClient, inFromServer; // For receiving from client and server
    private Socket clientSocket, serverSocket; // Sockets for client and server
    private String clientKey; // Key to store Socket in routing table

    public SThread(Hashtable<String, Socket> routingTable, Socket clientSocket) {
        this.routingTable = routingTable;
        this.clientSocket = clientSocket;
        this.clientKey = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();

        try {
            this.inFromClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.outToClient = new PrintWriter(this.clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up streams: " + e.getMessage());
        }
    }

    public void run() {
        // Store client connection to routing table
        routingTable.put(clientKey, clientSocket);

        // Wait until the TCPServer connection is available in the routing table
        while (serverSocket == null) {
            serverSocket = routingTable.get("server"); // Check if server exists
            if (serverSocket == null) {
                try {
                    Thread.sleep(1000); // Sleep for 1 second between checks
                } catch (InterruptedException e) {
                    System.err.println("Thread sleep interrupted: " + e.getMessage());
                }
            }
        }

        // Set up streams to and from server
        try {
            outToServer = new PrintWriter(serverSocket.getOutputStream(), true);
            inFromServer = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error setting up streams: " + e.getMessage());
        }

        // Communication loop
        try {
            String inputLine;
            while ((inputLine = inFromClient.readLine()) != null) {
                System.out.println("Received from Client: " + inputLine);

                // Forward client message to the server
                outToServer.println(inputLine);

                // Read response from server
                String response = inFromServer.readLine();
                System.out.println("Received from Server: " + response);

                // Send response back to client
                outToClient.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error in SThread communication loop: " + e.getMessage());
        } finally {
            // Clean up and remove disconnected client from routing table
            routingTable.remove(clientKey);

            System.out.println("SThread '" + Thread.currentThread().getId() + "' shutting down. Cleaning up connections.");

            // Close client connections
            try {
                if (inFromClient != null) inFromClient.close();
                if (outToClient != null) outToClient.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing sockets or streams: " + e.getMessage());
            }
        }
    }
}

