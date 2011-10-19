/**
 * @author Eduard Thamm
 * @matnr 0525087
 * @brief Abstract multithreaded server for DSLab.
 * @detail 
 */
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractServer {
    
    protected ServerSocket Ssock = null;
    protected ExecutorService e = Executors.newCachedThreadPool();
    protected int Tport;
    private final static boolean DEBUG = false;
    
    
    public abstract void tcpListen() throws IOException;
    
    public void exitRoutine(){
        e.shutdownNow();
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
        e.shutdownNow();
        try {
            Ssock.close();
        } catch (IOException e1) {
            System.out.print("Could not close ServerSocket. Exiting anyway.\n");
            if(DEBUG){e1.printStackTrace();}
            return;
        }
        return;
    }
    
    protected abstract class TCPListener extends Thread{
    }
    
    protected abstract class Worker extends Thread{
        
        protected Socket Csock;
        
        public Worker(Socket s) {
            super();
            Csock = s;
        }
        
        public void run(){
            System.out.print("This is a dummy run, which needs to be replaced by something useful.\n");
            return;
        }
    }
}
