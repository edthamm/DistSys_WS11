/**
 * 
 * @author Eduard Thamm
 * @matnr 0525087
 * @brief A Scheduling server handling TaskEngins and Clients
 * @detail This Server manages TEs by receiving their isAlive messages and querying them for data if necessary as well as suspending them if they are not needed.
 *  It also manages company data for accounting and billing purposes. 
 * This program was not written with security in mind 
 * !!!DO NOT USE IN PRODUCTIVE ENVIROMENT!!!
 */


import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;


public class Scheduler extends AbstractServer {
    
    private int uPort;
    private int minT;
    private int maxT;
    private int tout;
    private int checkP; //TODO ask about this what is it to do?
    private DatagramSocket uSock;
    private final static String usage = "Usage: Scheduler tcpPort udpPort min max tomeout checkPeriod\n";
    private List<GTEntry> GTs = Collections.synchronizedList(new LinkedList<GTEntry>());//needs to be threadsafe maybe Collections.synchronizedList(new LinkedList())
    private List<Company> Companies = Collections.synchronizedList(new LinkedList<Company>());
    private ExecutorService contE = Executors.newCachedThreadPool();
    private static final boolean DEBUG = true;
    
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
    
    public void readCompanies(){
        InputStream in = ClassLoader.getSystemResourceAsStream("company.properties");
        if(in != null){
            java.util.Properties companies = new java.util.Properties();
            try {
                companies.load(in);
                Set<String> companyNames = companies.stringPropertyNames();
                for (String companyName : companyNames){
                    String password = companies.getProperty(companyName);
                    Company c = new Company();
                    c.name = companyName;
                    c.password = password;
                    Companies.add(c);
                }
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                if(DEBUG){e.printStackTrace();}
            }
        }
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
            
            sched.readCompanies();
            sched.inputListen();
            sched.control();
            sched.listen();
            
            
        } catch (IOException e) {
            if(DEBUG){e.printStackTrace();}
        }
          catch(NumberFormatException e){
            System.out.print(usage + "All values must be integers.\n");
            if(DEBUG){e.printStackTrace();}
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
                if(DEBUG){e.printStackTrace();}
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
                if(DEBUG){e.printStackTrace();}
                System.exit(1);
            } 
            
            while(true){
                try {
                    uSock.receive(in);
                    contE.execute(new CWorker(in));
                } catch (IOException e) {
                    if(DEBUG){e.printStackTrace();}
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
        //set timer in gte call sr to update gt status on expire and reset with is alive in worker
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
                        int i = 1;
                        for (Company c : Companies){
                            System.out.print(i+". "+c.toString());
                            i++;
                        }                        
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
                if(DEBUG){e.printStackTrace();}
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
        Timer time;
        TimerTask TTASK;
        
        public GTEntry(String ip, int tcp, int udp, GTSTATUS status, int minE, int maxE , int load){
            this.ip = ip;
            this.tcp = tcp;
            this.udp = udp;
            this.status = status;
            this.minE = minE;
            this.maxE = maxE;
            this.load = load;            
        }
        
        //TODO this seems terribly inefficient
        public void startTimer(){
            time = new Timer();
            time.schedule(new Timeout(), tout);
        }
        
        public void stopTimer(){
            time.cancel();
        }
        
        public void resetTimer(){
            time.cancel();
            time = new Timer();
            time.schedule(new Timeout(), tout);
        }
        
        public String toString(){
            return("IP: "+ip+", TCP: "+tcp+", UDP: "+udp+", "+status.toString()+", Energy Signature: min "+minE+", max "+maxE+", Load: "+load+"%\n");
        }
        
        private class Timeout extends TimerTask{

            public void run() {
                status = GTSTATUS.offline;                
            }
            
        }
    }
    
    private class Company{
        private String name = null;
        private String password = null; //this is inherently unsafe in production use encryption
        private COMPANYCONNECT line = null;
        private int low = 0;
        private int middle = 0;
        private int high = 0;
        
        public String toString(){
            return(name+" ("+line.toString()+") LOW: "+low+", MIDDLE: "+middle+", HIGH: "+high+"\n");
        }
    }

}
