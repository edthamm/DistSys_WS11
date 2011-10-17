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
    private int checkP; //TODO wait for answer on this in the forum
    private int BUFSIZE = 1024;
    private Timer etime;
    private DatagramSocket uSock = null;
    private final static String usage = "Usage: Scheduler tcpPort udpPort min max tomeout checkPeriod\n";
    private List<GTEntry> GTs = Collections.synchronizedList(new LinkedList<GTEntry>());//needs to be threadsafe maybe Collections.synchronizedList(new LinkedList())
    private List<Company> Companies = Collections.synchronizedList(new LinkedList<Company>());
    private ExecutorService contE = Executors.newCachedThreadPool();
    private Controller c = null;
    private static final boolean DEBUG = false;
    
    public Scheduler(int tcpPort, int udpPort, int min, int max, int timeout, int checkPeriod){
        Tport = tcpPort;
        uPort = udpPort;
        minT = min;
        maxT = max;
        tout = timeout;
        checkP = checkPeriod;
    }
    
    private void control() {
        c = new Controller();
        c.start();        
    }
    
    public void readCompanies() throws FileNotFoundException{
        InputStream in = null;
        in = ClassLoader.getSystemResourceAsStream("company.properties");
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
                System.out.print("Could not read from company.properties. Exiting.\n");
                exitRoutineFail();
                if(DEBUG){e.printStackTrace();}
            }
        }
    }
    
    public GTEntry schedule(String t){//String is load.
        
        //TODO logic
        return null;
    }
    
    private void efficencyCheck(){
        etime = new Timer();
        etime.scheduleAtFixedRate(new ECheck(), checkP, checkP);
    }
    
    
    public boolean loggedIn(InetAddress in){
        for(Company c : Companies){
            if(c.via == in && c.line == COMPANYCONNECT.online){
                return true;
            }
        }
        return false;
    }
    
    
    public void exitRoutine(){
        contE.shutdownNow();
        if(uSock != null){uSock.close();}
        etime.cancel();
        super.exitRoutine();
        //System.exit(0);
        
    }
    
    public void exitRoutineFail(){
        contE.shutdownNow();
        if(uSock != null){uSock.close();}
        etime.cancel();
        super.exitRoutineFail();
        System.exit(1);
        
    }

    
    public static void main(String[] args) {
        
        if(args.length != 6){
            System.out.print(usage);
            System.exit(1);
        }
        
        try {
            Scheduler sched = new Scheduler(Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]),Integer.parseInt(args[5]));
            
            sched.readCompanies();
            //TODO catch fnf
            sched.inputListen();
            sched.control();
            sched.tcpListen();
            sched.efficencyCheck();
            
            
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
    

    @SuppressWarnings("unused")//called in superclass
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
                    output = processInput(input,Csock.getInetAddress());
                    out.println(output);
                }
                in.close();
                out.close();
                Csock.close();
                return;
            }
            catch(IOException e){
                if(DEBUG){e.printStackTrace();}
            }
        }
/*
 *  Preconditions: Client ensures that in !requestEngine only HIGH,MIDDLE,LOW are allowed on in[2], Companies not null
 *  Postconditions:
 */
        private String processInput(String input, InetAddress sender) {
            String[] in = input.split(" ");
            //TODO what if client dies set a timeout? ASK THIS
            if(!in[0].contains("!login") && !loggedIn(sender)){
                return "Please log in first.";
            }


            if(in[0].contentEquals("!requestEngine")){
                GTEntry g = schedule(in[2]);
                if(g == null){
                    return "Not enough capacity. Try again later.";
                }
                for(Company c : Companies){
                    if(c.via == sender){
                        if(in[2] == "HIGH"){
                            c.high++;
                            g.load = 100; //a high task will give 100% load but += would give more due to base load.
                        }
                        if(in[2] == "MIDDLE"){
                            c.middle++;
                            g.load += 66;
                        }
                        if(in[2] == "LOW"){
                            c.low++;
                            g.load += 33;
                        }
                    }
                }
                return "Assigned engine: "+g.ip+" "+g.tcp+" to task "+in[1];
                
                
            }
            if(in[0].contentEquals("!login")){
                for(Company c : Companies){
                    if(in[1].contentEquals(c.name) && in[2].contentEquals(c.password) && c.line != COMPANYCONNECT.online){
                        c.via = sender;
                        return "Successfully logged in.";
                    }
                    return"Wrong company or password.";
                }
                
            }
            if(in[0].contentEquals("!logout")){
                for(Company c : Companies){
                    if(c.via == sender){
                        c.line = COMPANYCONNECT.offline;
                        c.via = null;
                        break;
                    }
                }
                return "Successfully logged out.";   
            }
            
            
            return "Unrecognised message send !login,!logout, or !requestEngine."; //would not do this in production gives away to much info.
        }
        
    }
        
    
    //udp controlling of GTEs is done here
    
    

    private class Controller extends Thread{
        byte[] buf = new byte[BUFSIZE];
        private DatagramPacket in = new DatagramPacket(buf, buf.length);
        
        /*
         * Preconditions: none
         * Postconditions: a controller listens on the designated port and instantiates a worker for each new incoming GTE, or send msgs to tes
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
                } 
                catch (IOException e) {
                    if(DEBUG){e.printStackTrace();}
                    return;
                
                    
                }
            }
        }
        

    }
    
    private class CWorker extends Thread{
        //One CWorker manages on GTE
        private DatagramPacket in = null;
        public CWorker(DatagramPacket in) {
            this.in = in;
        }
        
        public CWorker(){
        	
        }
        
        public void run(){
            if(in == null){return;}// do a nice msg
        	for (GTEntry g : GTs){//TODO will this run on the first engine?
                if (g.ip == in.getAddress().toString()){ 
                    if(g.status != GTSTATUS.suspended){//ignore isAlives of suspended engines
                        g.resetTimer();
                        g.status = GTSTATUS.online;
                        return;
                    }
                    else{
                        return;
                    }
                }
            }
            String inString = new String(in.getData(), 0, in.getLength());
            String rcv[] = inString.split(" ");
            try{
                GTEntry g = new GTEntry(in.getAddress().toString(),Integer.parseInt(rcv[0]), in.getPort(), GTSTATUS.online, Integer.parseInt(rcv[1]), Integer.parseInt(rcv[2]), 0);
                GTs.add(g);
                g.startTimer();
            } catch(NumberFormatException e){
                System.out.print("An isAlive from a new TaskEngine is malformated. IP: "+in.getAddress().toString()+" \n");
                if(DEBUG){e.printStackTrace();}
            }
            
        }
        
        public void sendToTaskEngine(GTEntry g, String msg){
            byte[]  buf = new byte[BUFSIZE];
            buf = msg.getBytes();
            try {
              DatagramPacket p = new DatagramPacket(buf, buf.length,InetAddress.getByName(g.ip),g.udp);
              uSock.send(p);
          } catch (UnknownHostException e) {
              System.out.print("Could not send message to "+g.ip+" on UDP "+g.udp+" Host Unknown.\n");
              if(DEBUG){e.printStackTrace();}
          } catch (IOException e) {
              System.out.print("Could not send message to "+g.ip+" on UDP "+g.udp+" I/O Error.\n");
              if(DEBUG){e.printStackTrace();}
          }
          }
        
    }
    
    
    //Handling user input
    
    @SuppressWarnings("unused")//called in superclass
    private class InputListener extends AbstractServer.InputListener{
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
                        userin = "";
                    }
                    if(userin.contentEquals("!companies")){
                        int i = 1;
                        for (Company c : Companies){
                            System.out.print(i+". "+c.toString());
                            i++;
                        }
                        userin = "";
                        
                    }
                    if(userin.contentEquals("!exit")){
                        exitRoutine();
                        return;
                    }
                    if(userin != ""){
                        System.out.print("Unknown command.\n");
                    }
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
        
        public GTEntry(String ip, int tcp, int udp, GTSTATUS status, int minE, int maxE , int load){
            this.ip = ip;
            this.tcp = tcp;
            this.udp = udp;
            this.status = status;
            this.minE = minE;
            this.maxE = maxE;
            this.load = load;            
        }
        //TODO make sure this is ok
        public void startTimer(){
            time = new Timer();
            time.schedule(new Timeout(), tout);
        }
        
        public void stopTimer(){
            time.cancel();
        }
        
        public void resetTimer(){
            time.cancel(); //may be called repeatedly according to doc
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
        private InetAddress via = null;
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
    
    private class ECheck extends TimerTask{

        public void run() {
            int highUsers = 0;
            int emptyRunners = 0;
            int gtsUp = 0;
            for (GTEntry g : GTs){
                if(g.status == GTSTATUS.online){
                    gtsUp++;
                    if(g.load == 0){
                        emptyRunners++;
                    }
                    if(g.load > 65){
                        highUsers++;
                    }
                }
            }
            if((highUsers == gtsUp && gtsUp < maxT && gtsUp < GTs.size()) || (gtsUp < minT && gtsUp < GTs.size())){
                // no engine <66 load up and less than max engines active and inactive engines exist
                // active smaller min and suspended available
                GTEntry minEngine = GTs.get(0);
                for (GTEntry g :GTs){
                    if(minEngine.minE < g.minE && g.status == GTSTATUS.suspended){
                        minEngine = g;
                    }
                }
                activate(minEngine);
                //worst case first engine and that is offline well s happens
                //TODO check min == max
            }
            if(emptyRunners > 1 && gtsUp > minT){
                GTEntry maxEngine = GTs.get(0);
                for (GTEntry g :GTs){
                    if(maxEngine.maxE > g.maxE && g.status == GTSTATUS.online){
                        maxEngine = g;
                    }
                }
                suspend(maxEngine);
            }
            
        }
        
        private void suspend(GTEntry g){
            (new CWorker()).sendToTaskEngine(g, "!suspend");
            g.status = GTSTATUS.suspended;
            g.stopTimer();
        }
        
        private void activate(GTEntry g){
            (new CWorker()).sendToTaskEngine(g, "!wakeUp");
            g.status = GTSTATUS.offline; // will change to online once the first is alive is received cautious approach don't know if machine will respond
            
        }
    }

}
