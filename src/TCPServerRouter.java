import java.net.*;
import java.io.*;

public class TCPServerRouter {
    public static void main(String[] args) throws IOException {
        Socket clientSocket = null;
        Object[][] RoutingTable = new Object[10][2];
        int SockNum = 5555;
        boolean Running = true;
        int ind = 0;
        String localAddress = InetAddress.getLocalHost().getHostAddress();

        System.out.println("ServerRouter IP: " + localAddress);

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(SockNum);
            System.out.println("ServerRouter is Listening on port: " + SockNum);
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + SockNum);
            System.exit(1);
        }

        // Creating threads with accepted connections
        while (Running) {
            try {
                clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("New connection from: " + clientAddress);

                SThread t = new SThread(RoutingTable, clientSocket, ind);
                t.start();
                ind++;

                System.out.println("ServerRouter connected with Client/Server: " + clientAddress);
            } catch (IOException e) {
                System.err.println("Client/Server failed to connect.");
                System.err.println(e.getMessage());
                Running = false;
            }
        }

        if (clientSocket != null) {
            clientSocket.close();
            serverSocket.close();
        }
    }
}