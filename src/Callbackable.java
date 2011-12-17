import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Callbackable extends Remote {
    void sendMessage(String msg) throws RemoteException;
    void forceLogout() throws RemoteException;
    void handleResult(String msg, byte[] hash) throws RemoteException; //strictly speaking this is bad practice there should be an own if for this
}
