import java.io.*;
import java.net.*;
import java.util.Hashtable;

public class SThread extends Thread {
    private Hashtable<String, Socket> routingTable; // The routing table
    private ObjectOutputStream outToClient, outToServer; // For sending objects to client and server
    private ObjectInputStream inFromClient, inFromServer; // For receiving objects from client and server
    private Socket clientSocket, serverSocket; // Sockets for client and server
    private String clientKey; // Key to store Socket in routing table

    public SThread(Hashtable<String, Socket> routingTable, Socket clientSocket) {
        this.routingTable = routingTable;
        this.clientSocket = clientSocket;
        this.clientKey = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();

        try {
            this.inFromClient = new ObjectInputStream(this.clientSocket.getInputStream());
            this.outToClient = new ObjectOutputStream(this.clientSocket.getOutputStream());
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
            outToServer = new ObjectOutputStream(serverSocket.getOutputStream());
            inFromServer = new ObjectInputStream(serverSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error setting up streams: " + e.getMessage());
        }

        // Communication loop
        try {
            Object inputObject;
            while ((inputObject = inFromClient.readObject()) != null) {
                System.out.println("Received from Client: " + inputObject);

                // Forward client message to the server
                outToServer.writeObject(inputObject);
                outToServer.flush();

                // Read response from server
                Object response = inFromServer.readObject();
                System.out.println("Received from Server: " + response);

                // Send response back to client
                outToClient.writeObject(response);
                outToClient.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in SThread communication loop: " + e.getMessage());
        } finally {
            // Clean up and remove disconnected client from routing table
            routingTable.remove(clientKey);

            System.out.println(
                    "SThread '" + Thread.currentThread().getId() + "' shutting down. Cleaning up connections.");

            // Close client connections
            try {
                if (inFromClient != null)
                    inFromClient.close();
                if (outToClient != null)
                    outToClient.close();
                if (clientSocket != null)
                    clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing sockets or streams: " + e.getMessage());
            }
        }
    }
}
