import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Companyable extends Remote, Comunicatable{
    int getCredits() throws RemoteException;
    boolean buyCredits(int amount) throws RemoteException;
    boolean prepareTask(Task t) throws RemoteException;
    void executeTask(int id, String execln) throws RemoteException;
    void getTaskInfo(int id) throws RemoteException;
    void getOutputOf(int id) throws RemoteException;
    void logout() throws RemoteException;
}
