import java.io.IOException;
import java.io.*;
import java.net.*;

public class EchoServer {

    public static void main(String[] args) {
        try {
	    if (args.length != 1) {
		return;
	    }
	    
            ServerSocket listenSocket = new ServerSocket(Integer.parseInt(args[0]));
            while (true) {
                Socket clientSocket = listenSocket.accept(); 
		System.out.println("Accepted");
		DataInputStream in = new DataInputStream(
					new BufferedInputStream(clientSocket.getInputStream())); 
                // Connection c = new Connection(clientSocket);
		DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
		System.out.println("Waiting for message");
		byte[] message = new byte[1000];
		int size = in.read(message);
		String result = new String(message, 0, size); 
		System.out.println(result);
		out.writeUTF(result);
		

		in.close();
		out.close(); 
		clientSocket.close();
            }
            // listenSocket.close();
        } catch (IOException e) {
            System.out.println("Listen: " + e.getMessage());
        }
    }
}
/*
class Connection extends Thread {
    DataInputStream in; 
    DataOutputStream out; 
    Socket clientSocket; 

    public Connection(Socket aClientSocket) {
        try {
            System.out.println("connection established");
            this.clientSocket = aClientSocket;
            this.in = new DataInputStream(
		  new BufferedInputStream(this.clientSocket.getInputStream())); 
            this.out = new DataOutputStream(this.clientSocket.getOutputStream());
            this.start(); 
        } catch(IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
    }

    public void run() {
        try {
	    System.out.println("Data About to read");
	    byte message = this.in.readUTF();
            System.out.println("Data read");
            System.out.println(message);
            this.out.writeUTF(message);
        } catch(EOFException e) {
            System.out.println("EOF: " + e.getMessage());
        } catch(IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            try {
                this.clientSocket.close();
		this.in.close();
		this.out.close();
	    } catch(IOException e) {
                System.out.println("Closed Failed"); 
            }
        }
    }
}
*/
