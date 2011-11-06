import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Companyable extends Remote {
    int getCredits() throws RemoteException;
    boolean buyCredits(int amount) throws RemoteException;
    boolean prepareTask(Task t) throws RemoteException;
    boolean executeTask(int id) throws RemoteException;
    void getTaskInfo(int id) throws RemoteException;
    void getOutputOf(int id) throws RemoteException;
    void logout() throws RemoteException;
}
