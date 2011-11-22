import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Adminable extends Remote, Comunicatable {
    void getPrices() throws RemoteException;
    void setPrice(int step, double discount) throws RemoteException;
    void logout() throws RemoteException;
}
