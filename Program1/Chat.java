import java.net.*; // ServerSocket, Socket
import java.io.*; // InputStream, ObjectInputStream, ObjectOutputStream
import java.util.*; 


// Added by Gihwan Kwon
class Message implements Serializable {
	// Message class which is serializable to be passed over the network 
	// It contains time stamp vector, message, and the sender's rank. 

	private int[] stamp;
	private String message; 
	private int rank;
	
	// Message instantiated with senderRank, stamp, and message content 
	public Message(int rank, int[] stamp, String message) {
		this.deepCopy(stamp); 
		this.message = message; 
		this.rank = rank;
	}
	
	public int getRank() {
		return this.rank;
	}

	public int[] getStamp() {
		return this.stamp; 
	}

	public String getMessage() {
		return this.message;
	}

	// helper function that takes input stamp reference  
	// then, copy values to current message's stamp vector
	private void deepCopy(int[] stamp) {
		this.stamp = new int[stamp.length]; 
		for (int i = 0; i < stamp.length; i++) {
			this.stamp[i] = stamp[i]; 
		}
	}

}

public class Chat {
	// Each element i of the follwoing arrays represent a chat member[i]
	private Socket[] sockets = null; // connection to i
	private InputStream[] indata = null; // used to check data from i
	private ObjectInputStream[] inputs = null; // a message from i
	private ObjectOutputStream[] outputs = null; // a message to i
	
	// Added by Gihwan Kwon
	private int[] stamp;
	private ArrayList<Message> waitList; 
	private int rank; 

	/**
	 * Is the main body of the Chat application. This constructor establishes a
	 * socket to each remote chat member, broadcasts a local user's message to all
	 * the remote chat members, and receive a message from each of them.
	 *
	 * @param port  IP port used to connect to a remote node as well as to accept a
	 *              connection from a remote node.
	 * @param rank  this local node's rank (one of 0 through to #members - 1)
	 * @param hosts a list of all computing nodes that participate in chatting
	 */
	public Chat(int port, int rank, String[] hosts) throws IOException {
		// print out my port, rank and local hostname
		System.out.println("port = " + port + ", rank = " + rank + ", localhost = " + hosts[rank]);

		
		// Added by Gihwan Kwon
		// Initializing Vector stamps, current machine's rank, and waiting queue 
		this.stamp = new int[3];	// [0, 0, 0]
		this.rank = rank;
		this.waitList = new ArrayList<Message>();
		 

		// create sockets, inputs, outputs, and vector arrays
		sockets = new Socket[hosts.length];
		indata = new InputStream[hosts.length];
		inputs = new ObjectInputStream[hosts.length];
		outputs = new ObjectOutputStream[hosts.length];

		// establish a complete network
		ServerSocket server = new ServerSocket(port);
		for (int i = hosts.length - 1; i >= 0; i--) {
			if (i > rank) {
				// accept a connection from others with a higher rank
				Socket socket = server.accept();
				String src_host = socket.getInetAddress().getHostName();

				// find this source host's rank
				for (int j = 0; j < hosts.length; j++)
					if (src_host.startsWith(hosts[j])) {
						// j is this source host's rank
						System.out.println("accepted from " + src_host);

						// store this source host j's connection, input stream
						// and object intput/output streams.
						sockets[j] = socket;
						indata[j] = socket.getInputStream();
						inputs[j] = new ObjectInputStream(indata[j]);
						outputs[j] = new ObjectOutputStream(socket.getOutputStream());
					}
			}
			if (i < rank) {
				// establish a connection to others with a lower rank
				sockets[i] = new Socket(hosts[i], port);
				System.out.println("connected to " + hosts[i]);

				// store this destination host j's connection, input stream
				// and object intput/output streams.
				outputs[i] = new ObjectOutputStream(sockets[i].getOutputStream());
				indata[i] = sockets[i].getInputStream();
				inputs[i] = new ObjectInputStream(indata[i]);
			}
		}

		// create a keyboard stream
		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));


		// Loop modified by Gihwan Kwon
		// now goes into a chat
		while (true) {
			// read a message from keyboard and broadcast it to all the others.
			if (keyboard.ready()) {
				// since keyboard is ready, read one line.
				String message = keyboard.readLine();
				if (message == null) {
					// keyboard was closed by "^d"
					break; // terminate the program
				}


				
				// incrementing current rank's time stamp by one 
				this.stamp[rank]++;  

				// Create message packet with incremented stamp
				Message messagePacket = new Message(this.rank, this.stamp, message); 
				
				// broadcast a message to each of the chat members.
				for (int i = 0; i < hosts.length; i++)
					if (i != rank) {
						// of course I should not send a message to myself
						outputs[i].writeObject(messagePacket);
						outputs[i].flush(); // make sure the message was sent
					}
			}

			// read a message from each of the chat members
			for (int i = 0; i < hosts.length; i++) {
				// to intentionally create a misordered message deliveray,
				// let's slow down the chat member #2.
				try {
					if (rank == 2)
						Thread.currentThread().sleep(5000); // sleep 5 sec.
				} catch (InterruptedException e) {
				}

				// check if chat member #i has something
				if (i != rank && indata[i].available() > 0) {
					// read a message from chat member #i and print it out
					// to the monitor
					try {
						
						// Receive Message Packet 
						Message messagePacket = (Message) inputs[i].readObject(); 
						
						// String message = (String) inputs[i].readObject();
						// System.out.println(hosts[i] + ": " + message);

						// compare stamp (accpet only if stamp matches)
						if (this.compareStamp(messagePacket)) {
							System.out.println(hosts[i] + ": " + messagePacket.getMessage()); 
							this.stamp[i]++; 
						
							// check other message packet that can be accepted in the wait List
							checkWaitList(); 

						} else {
							// otherwise, put it in the waiting list (in FIFO order)
							this.waitList.add(messagePacket); 
						}
					} catch (ClassNotFoundException e) {
					}
				}
			}
		}
	}

	// Added by Gihwan Kwon
	private void checkWaitList() {
		ArrayList<Message> removingList = new ArrayList<Message>(); 
		for (int i = 0; i < this.waitList.size(); i++) {
			Message msg = this.waitList.get(i); 
			if (compareStamp(msg)) {
			    System.out.println("cssmpi" + (msg.getRank() + 1) + ": " + msg.getMessage()); 
				this.stamp[msg.getRank()]++; 

				removingList.add(msg); 
			}
		}

		// remove written Message from the waitList
		this.waitList.removeAll(removingList); 
	}

	

	// Added by GIhwan Kwon
	// return true, if sender stamp is in valid order 
	// return false, if sender stamp is not in valid order
	private Boolean compareStamp(Message messagePacket) {

		int[] senderStamp = messagePacket.getStamp(); 
		int senderRank = messagePacket.getRank(); 
		for (int currentRank = 0; currentRank < this.stamp.length; currentRank++) {
			if ( currentRank == senderRank ) { // sender rank 
				if (senderStamp[currentRank] <= this.stamp[currentRank]) { 
					// sender's stamp value smaller or equal to local stamp value  
					return false; 
				}
			}  else { // currentRank != senderRank 
				if (senderStamp[currentRank] > this.stamp[currentRank]) {
					return false; 
				}
				
			}
		}

		return true;
	}

	/**
	 * Is the main function that verifies the correctness of its arguments and
	 * starts the application.
	 *
	 * @param args receives <port> <ip1> <ip2> ... where port is an IP port to
	 *             establish a TCP connection and ip1, ip2, .... are a list of all
	 *             computing nodes that participate in a chat.
	 */
	public static void main(String[] args) {

		// verify #args.
		if (args.length < 2) {
			System.err.println("Syntax: java Chat <port> <ip1> <ip2> ...");
			System.exit(-1);
		}

		// retrieve the port
		int port = 0;
		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		if (port <= 5000) {
			System.err.println("port should be 5001 or larger");
			System.exit(-1);
		}

		// retireve my local hostname
		String localhost = null;
		try {
			localhost = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// store a list of computing nodes in hosts[] and check my rank
		int rank = -1;
		String[] hosts = new String[args.length - 1];
		for (int i = 0; i < args.length - 1; i++) {
			hosts[i] = args[i + 1];
			if (localhost.startsWith(hosts[i]))
				// found myself in the i-th member of hosts
				rank = i;
		}

		// now start the Chat application
		try {
			new Chat(port, rank, hosts);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
