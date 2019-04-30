import java.net.*;
import java.io.*;
import java.util.*;

// import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;


// Connection class creating network stream from input client socket
class Connection {
	private Socket clientSocket;   // socket connected to client 
	private DataInputStream in;    // input stream from client
	private DataOutputStream out;  // output stream to client
	private String name;           // name of the client 

	// Initializing connection 
	public Connection(Socket clientSocket) throws Exception {
		this.clientSocket = clientSocket;
		this.out = new DataOutputStream(this.clientSocket.getOutputStream());
		this.in = new DataInputStream(this.clientSocket.getInputStream());

		// 2. Read the client name with readUTF()
		this.name = this.in.readUTF();
	}

	// read Message from client and return message with client name
	public String readMessage() throws Exception {
		// (b) add the client name in front of the message received
		return this.name + ": " + this.in.readUTF();
	}

	// write messaage to the client 
	public void writeMessage(String message) throws Exception {
		this.out.writeUTF(message);
	}

	// check whether the message is available to read. 
	public Boolean isAvailable() throws Exception {
		return this.in.available() > 0;
	}
}

public class ChatServer {

	public static void main(String[] args) {
		try {
			if (args.length != 1) {
				System.err.println("Syntax: java ChatServer <port>");
				System.exit(1);
			}

			// Initializing List of connections
			ArrayList<Connection> connections = new ArrayList<Connection>();
			
			// Initializing List of connections that caused error 
			Set<Integer> errorList = new HashSet<Integer>(); 

			// Initializing Server socket listening to the user input port 
			ServerSocket listenSock = new ServerSocket(Integer.parseInt(args[0]));
			
			// preventtion from accept() blocking
			listenSock.setSoTimeout(500); 
			
			Socket clientSock = null; 
			while (true) {
				try {
					// 1. Accept a new client socket connection if there is any
					clientSock = listenSock.accept();
					
					// 3. Add connection into the list of existing client pool
					if (clientSock != null) {
						connections.add(new Connection(clientSock));
						System.out.println("Connection Established");
					}

				} catch (SocketTimeoutException e) {
					// Expected Error ! 
					// so ignore
				} finally {
					// 4. for each connection of the list, 
					for (int i = 0; i < connections.size(); i++) {
						try {
							Connection c = connections.get(i);
							// (a) receive a new message, if available
							if (c.isAvailable()) {
								String message = c.readMessage();
								System.out.println(message);
								
								// (c) write this message to all clients through all connections
								// of the list. Use writeUTF();
								sendAll(connections, message, errorList);
							}

							
						} catch (Exception e) {
							// current connection caused error, so add to error list  
							errorList.add(i); 
						}
					}
					
					// delete connections that caused error 
					deleteErrorConnections(connections, errorList);
				}
			}

			// listenSock.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void deleteErrorConnections(ArrayList<Connection> connections, Set<Integer> errorList) {
		// System.out.println("error List = " + errorList.size()); 
		// remove connections from the connection list which appear in the errorList 
		for (int index: errorList) {
			connections.remove(index); 
		}
		// System.out.println("Client List = " + connections.size());

		// clearing errorList
		errorList.clear();
	}


	public static void sendAll(ArrayList<Connection> connections, String message, Set<Integer> errorList) {		
		// send message to all clients
		for (int i = 0; i < connections.size(); i++) {
			try {
				connections.get(i).writeMessage(message);
			}  catch (Exception e) {
				// exception while writing message, so adding connection to the error list
				errorList.add(i); 
			}
		}
	}
}
