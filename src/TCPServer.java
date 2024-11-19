import java.io.*;
import java.net.*;

public class TCPServer {
    public static void main(String[] args) {
        String routerIP = "localhost";
        int routerPort = 5555;
        Socket routerSocket = null;
        ObjectOutputStream objectOut = null;
        ObjectInputStream objectIn = null;

        try {
            String serverAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server IP: " + serverAddress);

            // Connect to the local router
            routerSocket = new Socket(routerIP, routerPort);
            System.out.println("Connected to router socket");

            // Create output stream first
            System.out.println("Creating output stream...");
            objectOut = new ObjectOutputStream(routerSocket.getOutputStream());
            objectOut.flush();
            System.out.println("Output stream created and flushed");

            // Create input stream
            System.out.println("Creating input stream...");
            objectIn = new ObjectInputStream(routerSocket.getInputStream());
            System.out.println("Input stream created");

            // Send identification
            System.out.println("Sending SERVER identification...");
            objectOut.writeObject("SERVER");
            objectOut.flush();
            System.out.println("SERVER identification sent");

            // Receive confirmation
            System.out.println("Waiting for router confirmation...");
            String confirmation = (String)objectIn.readObject();
            System.out.println("Router response: " + confirmation);

            boolean running = true;
            while (running) {
                try {
                    System.out.println("Waiting for incoming message...");
                    Object incoming = objectIn.readObject();
                    System.out.println("Received message of type: " + (incoming != null ? incoming.getClass().getSimpleName() : "null"));

                    if (incoming instanceof String) {
                        String command = (String) incoming;
                        System.out.println("Received command: " + command);

                        if ("Start".equals(command)) {
                            System.out.println("Received Start command, waiting for matrices...");
                            Object matricesObj = objectIn.readObject();
                            System.out.println("Received matrices object of type: " +
                                    (matricesObj != null ? matricesObj.getClass().getSimpleName() : "null"));

                            if (matricesObj instanceof matrix[]) {
                                matrix[] matrices = (matrix[]) matricesObj;
                                System.out.println("Processing " + matrices.length + " matrices...");

                                // Process matrices
                                int[][] result = MatrixFileIO.resultMatrix(matrices);
                                matrix finalMatrix = new matrix(result);

                                System.out.println("Sending result back to client...");
                                objectOut.writeObject(finalMatrix);
                                objectOut.flush();
                                System.out.println("Result sent");
                            }
                        } else if ("Bye.".equals(command)) {
                            System.out.println("Received Bye command, ending session");
                            running = false;
                        }
                    }
                } catch (EOFException e) {
                    System.out.println("Client disconnected (EOF)");
                    break;
                } catch (SocketException e) {
                    System.out.println("Socket exception: " + e.getMessage());
                    break;
                } catch (StreamCorruptedException e) {
                    System.out.println("Stream corrupted: " + e.getMessage());
                    System.out.println("Available bytes: " + routerSocket.getInputStream().available());
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Cleaning up server resources...");
            try {
                if (objectOut != null) {
                    System.out.println("Closing output stream...");
                    objectOut.close();
                }
                if (objectIn != null) {
                    System.out.println("Closing input stream...");
                    objectIn.close();
                }
                if (routerSocket != null) {
                    System.out.println("Closing socket...");
                    routerSocket.close();
                }
                System.out.println("Cleanup complete");
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}