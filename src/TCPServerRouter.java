import java.net.*;
import java.io.*;
import java.util.Hashtable;

public class TCPServerRouter {
    public static void main(String[] args) {
        int sockNum = 5555; // Port for ServerRouter
        ServerSocket serverSocket = null;
        Hashtable<String, Socket> routingTable = new Hashtable<>(); // Routing table

        try {
            serverSocket = new ServerSocket(sockNum);
            System.out.println("ServerRouter is listening on port: " + sockNum);

            // First, accept connection from TCPServer
            Socket serverConnection = serverSocket.accept();
            System.out.println("Server connected to ServerRouter");

            // Store TCPServer connection to the routing table with key "server"
            routingTable.put("server", serverConnection);

            // Accept connections from TCPClient(s)
            boolean running = true;
            while (running) {
                try {
                    // Accept new client connection
                    Socket clientSocket = serverSocket.accept();
                    String clientIP = clientSocket.getInetAddress().getHostAddress();
                    int clientPort = clientSocket.getPort();
                    System.out.println("Client connected: " + clientIP + ":" + clientPort);

                    // Create a new thread for handling communication with client
                    SThread thread = new SThread(routingTable, clientSocket);
                    thread.start();
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                    running = false;
                }
            }
        } catch (IOException e) {
            System.err.println("ServerRouter encountered an error: " + e.getMessage());
        } finally {
            // Clean up by closing all connections
            System.out.println("ServerRouter shutting down. Cleaning up connections.");

            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }

                for (Socket socket : routingTable.values()) {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                }
            } catch (IOException e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
    }
}
