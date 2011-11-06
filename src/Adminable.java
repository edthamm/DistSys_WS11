import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.Map.Entry;


public interface Adminable extends Remote, Comunicatable {
    Set<Entry<Integer, Double>> getPrices() throws RemoteException;
    void setPrice(int step, double discount) throws RemoteException;
    void logout() throws RemoteException;
}
