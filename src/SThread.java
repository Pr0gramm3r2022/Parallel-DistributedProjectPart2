import java.io.*;
import java.net.*;

public class SThread extends Thread {
	private Object[][] RTable;
	private String addr;
	private Socket clientSocket;
	private int ind;
	private ObjectOutputStream objectOut;
	private ObjectInputStream objectIn;
	private SThread serverThread;
	private SThread clientThread;
	private boolean isServer = false;
	private volatile boolean running = true;

	SThread(Object[][] Table, Socket toClient, int index) throws IOException {
		RTable = Table;
		clientSocket = toClient;
		addr = toClient.getInetAddress().getHostAddress();
		ind = index;

		RTable[index][0] = addr;
		RTable[index][1] = this;

		objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
		objectOut.flush();
		objectIn = new ObjectInputStream(clientSocket.getInputStream());
	}

	public void run() {
		try {
			System.out.println("[Thread-" + ind + "] Handling connection for: " + addr);

			// Handle initial connection
			Object initialMessage = objectIn.readObject();
			System.out.println("[Thread-" + ind + "] Received initial message: " + initialMessage);

			if (initialMessage instanceof String) {
				String msg = (String)initialMessage;
				if ("SERVER".equals(msg)) {
					isServer = true;
					System.out.println("[Thread-" + ind + "] Server connected");
				} else {
					System.out.println("[Thread-" + ind + "] Client requesting server at: " + msg);
				}
			}

			objectOut.writeObject("Connected to the router.");
			objectOut.flush();

			if (!isServer) {
				findServerThread();
				if (serverThread != null) {
					serverThread.setClientThread(this);
				}
			}

			// Main message handling loop
			while (running && !clientSocket.isClosed()) {
				try {
					Object message = objectIn.readObject();
					if (message == null) break;

					System.out.println("[Thread-" + ind + "] Received message type: " +
							message.getClass().getSimpleName());

					if (isServer) {
						handleServerMessage(message);
					} else {
						handleClientMessage(message);
					}

					if ("Bye.".equals(message)) {
						System.out.println("[Thread-" + ind + "] Received Bye message, closing connection");
						break;
					}
				} catch (EOFException | SocketException e) {
					// Normal socket closure
					System.out.println("[Thread-" + ind + "] Connection closed by " +
							(isServer ? "server" : "client"));
					break;
				} catch (IOException e) {
					if (!running) break;
					System.err.println("[Thread-" + ind + "] IO Error: " + e.getMessage());
					break;
				}
			}
		} catch (Exception e) {
			if (running) {
				System.err.println("[Thread-" + ind + "] Unexpected error: " + e.getMessage());
				e.printStackTrace();
			}
		} finally {
			cleanup();
		}
	}

	private void findServerThread() {
		for (int i = 0; i < RTable.length; i++) {
			if (RTable[i][1] != null && i != ind) {
				SThread potentialServer = (SThread) RTable[i][1];
				if (potentialServer.isServer) {
					serverThread = potentialServer;
					System.out.println("[Thread-" + ind + "] Found server at index: " + i);
					return;
				}
			}
		}
		System.err.println("[Thread-" + ind + "] No server found in routing table");
	}

	public void setClientThread(SThread client) {
		this.clientThread = client;
		System.out.println("[Thread-" + ind + "] Set client thread: " + client.ind);
	}

	private void handleServerMessage(Object message) {
		try {
			if (message instanceof matrix[] || message instanceof matrix) {
				System.out.println("[Thread-" + ind + "] Server processing message: " +
						message.getClass().getSimpleName());

				if (clientThread != null) {
					System.out.println("[Thread-" + ind + "] Server forwarding response to client thread: " +
							clientThread.ind);
					clientThread.objectOut.writeObject(message);
					clientThread.objectOut.flush();
					System.out.println("[Thread-" + ind + "] Server sent response to client");
				} else {
					System.err.println("[Thread-" + ind + "] No client thread to send response to!");
				}
			}
		} catch (IOException e) {
			System.err.println("[Thread-" + ind + "] Error in server handling: " + e.getMessage());
		}
	}

	private void handleClientMessage(Object message) {
		if (serverThread == null) return;

		try {
			serverThread.objectOut.writeObject(message);
			serverThread.objectOut.flush();
			System.out.println("[Thread-" + ind + "] Forwarded to server: " +
					message.getClass().getSimpleName());
		} catch (IOException e) {
			System.err.println("[Thread-" + ind + "] Error handling client message: " + e.getMessage());
		}
	}

	private void cleanup() {
		running = false;
		try {
			if (objectOut != null) objectOut.close();
			if (objectIn != null) objectIn.close();
			if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();

			// Clear references
			RTable[ind][1] = null;
			if (isServer) {
				clientThread = null;
			} else {
				serverThread = null;
			}

			System.out.println("[Thread-" + ind + "] Cleanup complete");
		} catch (IOException e) {
			System.err.println("[Thread-" + ind + "] Error in cleanup: " + e.getMessage());
		}
	}
}