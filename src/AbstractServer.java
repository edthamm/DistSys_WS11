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
    
    private ServerSocket Ssock = null;
    private ExecutorService e = Executors.newCachedThreadPool();
    protected int port;
    private final static boolean DEBUG = false;
    
    
    public void listen() throws IOException{
        Listener l = new Listener();
        l.start();

    }
    
    public void exitRoutine(){
        e.shutdownNow();
        try {
            Ssock.close();
        } catch (IOException e1) {
            System.out.print("Could not close ServerSocket. Exiting anyway.\n");
            if(DEBUG){e1.printStackTrace();}
            System.exit(1);
        }
        //System.exit(0);
    }
    public void exitRoutineFail(){
        e.shutdownNow();
        try {
            Ssock.close();
        } catch (IOException e1) {
            System.out.print("Could not close ServerSocket. Exiting anyway.\n");
            if(DEBUG){e1.printStackTrace();}
            System.exit(1);
        }
        System.exit(1);
    }
    
    protected class Listener extends Thread{
        public void run(){
            
            try {
                Ssock = new ServerSocket(port);
            } catch (IOException e) {
                System.out.print("Could not listen on port: " + port + "\n");
                if(DEBUG){e.printStackTrace();}
                System.exit(1);
            }
            
            while(true){
                try {
                    e.execute(new Worker(Ssock.accept()));
                } catch (IOException e) {
                    if(DEBUG){e.printStackTrace();}
                    return;
                }
            }
        }
        
    }
    
    protected class Worker extends Thread{
        
        protected Socket Csock;
        
        public Worker(Socket s) {
            super();
            Csock = s;
        }
        
        public void run(){
            System.out.print("This is a dummy run, which needs to be replaced by something useful.\n");
        }
    }

}
