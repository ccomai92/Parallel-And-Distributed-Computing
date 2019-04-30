import java.rmi.*; 
import java.rmi.server.*;

public class ClientImplementation extends UnicastRemoteObject implements ClientInterface {
    static private int i = 0;
    public ClientImplementation() throws RemoteException {
        
    }

    public void receiveMessage(String message) throws RemoteException  {
        System.out.println(message); 
    }
}