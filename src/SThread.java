import java.io.*;
import java.net.*;

public class SThread extends Thread {
	private Object[][] RTable;
	private String addr;
	private Socket outSocket;
	private int ind;
	private ObjectOutputStream objectOut;
	private ObjectInputStream objectIn;
	private ObjectOutputStream destObjectOut;
	private ObjectInputStream destObjectIn;
	private boolean isServer = false;
	private Socket clientSocket;

	SThread(Object[][] Table, Socket toClient, int index) throws IOException {
		RTable = Table;
		clientSocket = toClient;
		addr = toClient.getInetAddress().getHostAddress();
		ind = index;

		// Store in routing table
		RTable[index][0] = addr;
		RTable[index][1] = toClient;
	}

	public void run() {
		try {
			// Initialize streams for incoming connection - IMPORTANT: Output before Input
			objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
			objectOut.flush();
			objectIn = new ObjectInputStream(clientSocket.getInputStream());

			// Read initial identification
			Object initialMessage = objectIn.readObject();
			if (initialMessage == null) {
				System.err.println("Error: Received null initial message");
				return;
			}

			if (initialMessage instanceof String) {
				String msg = (String)initialMessage;
				if ("SERVER".equals(msg)) {
					isServer = true;
					System.out.println("Server connected at index: " + ind);
				} else {
					System.out.println("Client requesting server at: " + msg);
				}
			}

			// Send confirmation
			objectOut.writeObject("Connected to the router.");
			objectOut.flush();

			// If this is a client connection, find the server
			if (!isServer) {
				System.out.println("Looking for server connection...");
				sleep(1000);

				for (int i = 0; i < RTable.length; i++) {
					if (RTable[i][1] != null && i != ind) {
						Socket potentialServer = (Socket) RTable[i][1];
						if (potentialServer.isConnected() && !potentialServer.isClosed()) {
							outSocket = potentialServer;
							System.out.println("Found server at index: " + i);
							break;
						}
					}
				}

				if (outSocket == null) {
					System.err.println("No server found in routing table");
					return;
				}

				// Initialize server connection streams - IMPORTANT: Output before Input
				destObjectOut = new ObjectOutputStream(outSocket.getOutputStream());
				destObjectOut.flush();
				destObjectIn = new ObjectInputStream(outSocket.getInputStream());
				System.out.println("Server connection streams initialized");
			}

			// Message forwarding loop
			while (!clientSocket.isClosed()) {
				try {
					Object message = objectIn.readObject();
					System.out.println("Received: " + message.getClass().getSimpleName());

					if (!isServer && destObjectOut != null) {
						// Forward to server
						destObjectOut.writeObject(message);
						destObjectOut.flush();

						// If it's matrices, wait for response
						if (message instanceof matrix[]) {
							Object response = destObjectIn.readObject();
							objectOut.writeObject(response);
							objectOut.flush();
						}
					}

					if (message instanceof String && "Bye.".equals(message)) {
						break;
					}
				} catch (EOFException e) {
					System.out.println("Connection ended by peer");
					break;
				} catch (SocketException e) {
					System.out.println("Socket connection terminated");
					break;
				}
			}
		} catch (Exception e) {
			System.err.println("Error in routing thread: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (objectOut != null) objectOut.close();
				if (objectIn != null) objectIn.close();
				if (destObjectOut != null) destObjectOut.close();
				if (destObjectIn != null) destObjectIn.close();
				if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
			} catch (IOException e) {
				System.err.println("Error closing resources: " + e.getMessage());
			}
		}
	}
}
