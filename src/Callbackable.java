import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Callbackable extends Remote {
    void sendMessage(String msg) throws RemoteException;
}
