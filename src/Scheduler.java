/**
 * 
 */


import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.concurrent.*;


public class Scheduler extends AbstractServer {
    
    private int uPort;
    private int minT;
    private int maxT;
    private int tout;
    private int checkP;
    private DatagramSocket uSock;
    private final static String usage = "Usage: Scheduler tcpPort udpPort min max tomeout checkPeriod\n";
    private LinkedList<GTEntry> GTs = new LinkedList<GTEntry>();//needs to be threadsafe maybe Collections.synchronizedList(new LinkedList())
    private ExecutorService contE = Executors.newCachedThreadPool();
    
    public Scheduler(int tcpPort, int udpPort, int min, int max, int timeout, int checkPeriod){
        port = tcpPort;
        uPort = udpPort;
        minT = min;
        maxT = max;
        tout = timeout;
        checkP = checkPeriod;
    }
    
    private void control() {
        Controller c = new Controller();
        c.run();        
    }
    
    public void inputListen(){
        InputListener i = new InputListener();
        i.run();        
    }
    
    public int schedule(){
        int i = 0;
        
        return i;
    }
    
    public void exitRoutine(){
        contE.shutdownNow();
        uSock.close();
        super.exitRoutine();
        System.exit(0);
        
    }

    
    public static void main(String[] args) {
        
        if(args.length != 6){
            System.out.print(usage);
            System.exit(1);
        }
        
        try {
            Scheduler sched = new Scheduler(Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]),Integer.parseInt(args[5]));
            
            sched.inputListen();
            sched.control();
            sched.listen();
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
          catch(NumberFormatException e){
            System.out.print(usage + "All values must be integers.\n");
            e.printStackTrace();
            System.exit(1);
        }

    }
    

    // Client handling is done in worker.
    

    private class Worker extends AbstractServer.Worker{

        public Worker(Socket s) {
            super(s);
        }
        
        public void run(){
            try{
                PrintWriter out = new PrintWriter(Csock.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(Csock.getInputStream()));
                
                String input, output;
                
                while((input = in.readLine()) != null){
                    output = processInput(input);
                    out.println(output);
                    //TODO ad termination on bye
                }
                in.close();
                out.close();
                Csock.close();
                
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        private String processInput(String input) {
            // TODO logic
            return null;
        }
        
    }
        
    
    //udp controlling of GTEs is done here
    
    

    private class Controller extends Thread{
        byte[] buf = new byte[1024];
        private DatagramPacket in = new DatagramPacket(buf, buf.length);
        
        /*
         * Preconditions: none
         * Postconditions: a controller listens on the designated port and instantiates a worker for each new incoming GTE
         */
        public void run(){
            try {
                uSock = new DatagramSocket(uPort);
                
            } catch (SocketException e) {
                System.out.print("Unable to listen on UDP "+uPort+"\n");
                e.printStackTrace();
                System.exit(1);
            } 
            
            while(true){
                //TODO i dont get this, will this work ||
                try {
                    uSock.receive(in);
                    contE.execute(new CWorker(in));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private class CWorker extends Thread{
        //One CWorker manages on GTE
        private DatagramPacket in;
        //TODO logic
        public CWorker(DatagramPacket in) {
            this.in = in;
        }
        //set timer in gte call sr to update gt status on expire and reset with s alive in worker
    }
    
    
    //Handling user input
    
    private class InputListener extends Thread{
        public void run(){
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String userin;
            
            try {
                while((userin = stdin.readLine()) != null){
                    if(userin.contentEquals("!engines")){
                        int i = 1;
                        for(GTEntry p: GTs){
                            System.out.print(i+". "+p.toString());
                            i++;
                        }                    
                        break;
                    }
                    if(userin.contentEquals("!companies")){
                        
                        break;
                    }
                    if(userin.contentEquals("!exit")){
                        exitRoutine();
                        break;
                    }
                    System.out.print("Unknown command.\n");
                    return;
                }
            } catch (IOException e) {
                System.out.print("Could not read from stdin.\n");
                e.printStackTrace();
                System.exit(1);
            }
        }
        
    }
    private class GTEntry{
        private String ip;
        private int tcp;
        private int udp;
        private GTSTATUS status;
        private int minE;
        private int maxE;
        private int load;
        
        public GTEntry(String ip, int tcp, int udp, GTSTATUS status, int minE, int maxE , int load){
            this.ip = ip;
            this.tcp = tcp;
            this.udp = udp;
            this.status = status;
            this.minE = minE;
            this.maxE = maxE;
            this.load = load;            
        }
        
        public String toString(){
            return("IP: "+ip+", TCP: "+tcp+", UDP: "+udp+", "+status.toString()+", Energy Signature: min "+minE+", max "+maxE+", Load: "+load+"%\n");
        }
    }

}
