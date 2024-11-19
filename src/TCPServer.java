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

            // Create object streams first - IMPORTANT: Output must be created before Input
            objectOut = new ObjectOutputStream(routerSocket.getOutputStream());
            objectOut.flush();
            objectIn = new ObjectInputStream(routerSocket.getInputStream());

            // Send identification
            objectOut.writeObject("SERVER");
            objectOut.flush();

            // Receive confirmation
            String confirmation = (String)objectIn.readObject();
            System.out.println("Router response: " + confirmation);

            boolean running = true;
            while (running) {
                try {
                    Object incoming = objectIn.readObject();
                    System.out.println("Received: " + (incoming != null ? incoming.getClass().getSimpleName() : "null"));

                    if (incoming instanceof String) {
                        String command = (String) incoming;
                        if ("Start".equals(command)) {
                            // Expect matrices next
                            Object matricesObj = objectIn.readObject();
                            if (matricesObj instanceof matrix[]) {
                                matrix[] matrices = (matrix[]) matricesObj;
                                System.out.println("Processing " + matrices.length + " matrices...");

                                // Process matrices
                                int[][] result = MatrixFileIO.resultMatrix(matrices);
                                matrix[] resultArray = new matrix[]{new matrix(result)};

                                // Send result back
                                objectOut.writeObject(resultArray);
                                objectOut.flush();
                                System.out.println("Sent result back to client");
                            }
                        } else if ("Bye.".equals(command)) {
                            running = false;
                        }
                    }
                } catch (EOFException | SocketException e) {
                    System.out.println("Client disconnected");
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (objectOut != null) objectOut.close();
                if (objectIn != null) objectIn.close();
                if (routerSocket != null) routerSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}