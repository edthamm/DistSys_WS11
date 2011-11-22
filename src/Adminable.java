import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.Map.Entry;


public interface Adminable extends Remote, Comunicatable {
    void getPrices() throws RemoteException;
    void setPrice(int step, double discount) throws RemoteException;
    void logout() throws RemoteException;
}
