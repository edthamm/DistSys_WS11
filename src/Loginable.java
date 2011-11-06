import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Loginable extends Remote {
    Comunicatable login(String uname, String password, Callbackable cb) throws RemoteException;
}
