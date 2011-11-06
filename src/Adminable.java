import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;


public interface Adminable extends Remote {
    Set<Map.Entry<Integer, Integer>> getPrices() throws RemoteException;//TODO get correct type
    void setPrice(int step, int discount) throws RemoteException;
    void logout() throws RemoteException;
}
