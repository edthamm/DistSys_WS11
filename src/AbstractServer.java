/**
 * @author Eduard Thamm
 * @matnr 0525087
 * @brief Abstract multithreaded server for DSLab.
 * @detail 
 */
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractServer {
    
    protected ServerSocket Ssock = null;
    protected ExecutorService abservexe = Executors.newCachedThreadPool();
    protected int Tport;
    private final static boolean DEBUG = false;
    protected LinkedList<Socket> CSocks = new LinkedList<Socket>();
    
    
    public abstract void tcpListen() throws IOException;
    
    public void exitRoutine(){
        abservexe.shutdownNow();
        try {
            Ssock.close();
        } catch (IOException e1) {
            System.out.print("Could not close ServerSocket. Exiting anyway.\n");
            if(DEBUG){e1.printStackTrace();}
            return;
        }
        return;
    }
    public void exitRoutineFail(){
        abservexe.shutdownNow();
        try {
            Ssock.close();
        } catch (IOException e1) {
            System.out.print("Could not close ServerSocket. Exiting anyway.\n");
            if(DEBUG){e1.printStackTrace();}
            return;
        }
        return;
    }
        
    protected abstract class Worker extends Thread{
        
        protected Socket Csock;
        
        public Worker(Socket s) {
            super();
            Csock = s;
            CSocks.add(s);
        }
        
        public void run(){
            System.out.print("This is a dummy run, which needs to be replaced by something useful.\n");
            return;
        }
    }
}
