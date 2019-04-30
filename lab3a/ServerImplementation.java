import java.rmi.*;
import java.rmi.server.*;


public class ServerImplementation extends UnicastRemoteObject implements ServerInterface {
    static private int i = 0; 
    
    public ServerImplementation() throws RemoteException {

    }

    public void echo(ClientInterface client, String message) throws RemoteException {
	    client.receiveMessage(message);
    }
}
