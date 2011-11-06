/**
 * @author Eduard Thamm
 * @matnr 0525087
 * @brief Client program for DSLab.
 * @detail Simple Client for the DsLab performs various activities like logging in. requesting
 * resources and some things more. This program was not written with security in mind 
 * !!!DO NOT USE IN PRODUCTIVE ENVIROMENT!!!
 */

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.concurrent.*;

public class Client {
    
    private int port;
    private String server;
    private Socket ssock;
    private PrintWriter sout;
    private BufferedReader sin;
    private File tdir;
    private ExecutorService e = Executors.newCachedThreadPool();
    private static final boolean DEBUG = false;

    /*
     * Preconditions: srv, p not null
     * Postconditions: new Client is created server and port are set
     */
    public Client(String srv, int p, String dir){
        port = p;
        server = srv;
        tdir = new File(dir);
        if (!tdir.exists()){
            System.out.print(tdir.getName()+"does not exists.\n");
            System.exit(1);
        }
        if(!tdir.isDirectory()){
            System.out.print(tdir.getName()+"is not a directory.\n");
            System.exit(1);
        }
    }
    
    
    private void startUp(){
        try {
            ssock = new Socket(server, port);
            sout = new PrintWriter(ssock.getOutputStream(), true);
            sin = new BufferedReader(new InputStreamReader(ssock.getInputStream()));
        } catch (UnknownHostException e) {
            System.out.print("Login: Unknown Host, check server name and port.\n");
            if(DEBUG){e.printStackTrace();}
            System.exit(1);
        } catch (IOException e) {
            System.out.print("Login: Could not get I/O for "+server+" \n");
            if(DEBUG){e.printStackTrace();}
            System.exit(1);
        }
    }
    /*
     * Preconditions: none
     * Postconditions: program execution started
     */
    public void run() throws IOException{
        BufferedReader stdin = new BufferedReader( new InputStreamReader(System.in));
        String userin;
        boolean exitFlag = false;
        
        while(!exitFlag && (userin = stdin.readLine()) != null){
            String usercmd[] = userin.split("\"");
            String usersp[] = usercmd[0].split(" ");
            if(usercmd.length > 1){
                String tmp[] = new String[3];
                tmp[0] = usersp[0];
                tmp[1] = usersp[1];
                tmp[2] = usercmd[1]; 
                usersp = tmp;
            }
            try{
                exitFlag = checkAction(usersp);
            }
            catch(NumberFormatException e){
                System.out.print("You entered a non integer value. Please enter an Integer value.\n");
            }
        }
        return;
    
    }
    
    /*
     * Preconditions: user input received
     * Postconditions: user input processed and handling function called accordingly
     */
    private boolean checkAction(String[] in) throws NumberFormatException, IOException{
        
        if(in[0].contentEquals("!login")){
            if(in.length != 3){
                System.out.print("Invalid parameters. Usage: !login username password.\n");
                return false;
            }
            login(in[1],in[2]);
            return false; 
        }
        if(in[0].contentEquals("!logout")){
            logout();
            return false; 
        }        
        if(in[0].contentEquals("!list")){
            list();
            return false; 
        }
        if(in[0].contentEquals("!prepare")){
            if(in.length != 3){
               System.out.print("Invalid parameters. Usage: !prepare taskname tasktype.\n");
               return false;
            }
            prepare(in[1],in[2]);
           return false; 
        }
        if(in[0].contentEquals("!executeTask")){
            if(in.length != 3){
                System.out.print("Invalid parameters. Usage: !executeTask taskid startscript.\n");
                return false;
            } 
            executeTask(Integer.parseInt(in[1]),in[2]);
           return false; 
        }
        if(in[0].contentEquals("!info")){
            if(in.length != 2){
                System.out.print("Invalid parameters. Usage: !info taskid.\n");
                return false;
            }
            info(Integer.parseInt(in[1]));
            return false; 
        }
        if(in[0].contentEquals("!exit")){
            exit(); 
            return true;
        }
        else{
            System.out.print("Command not recognised.\n");
            return false;
        }
    }
    
    
    // Scheduler Connection up/down
    
    
    
    /*
     * Preconditions: user, pass not null
     * Postconditions: Connection is build, reading and writing lines are opened. User is logged in to server, or error is thrown.
     */
    private void login(String user, String pass){
        sout.println("!login "+user+" "+pass);
        e.execute(new Listener(ssock,sin));
    }
    
    /*
     * Preconditions: logged in
     * Postconditions: logged out
     */
    private void logout(){
        if(sout == null){
            System.out.print("Must be logged in.\n");
        }
        else{
            sout.println("!logout");
        }
    }
    
    
    // Task life cycle
    
    
    /*
     * Preconditions: none
     * Postconditions: new task with unique taskid prepared
     */
    private void prepare(String task, String type) throws IOException{        
        TASKTYPE typ = null;
        if(type.contentEquals("LOW")){
            typ = TASKTYPE.LOW;
            
        }
        else{
            if(type.contentEquals("MIDDLE")){
                typ = TASKTYPE.MIDDLE;
            }
            else{
                if(type.contentEquals("HIGH")){
                    typ = TASKTYPE.HIGH;
                }
        
                else{
                    System.out.print("Invalid type. Use [LOW|MIDDLE|HIGH].\n");
                    return;
                }
            }
        }
        File f = new File(tdir.getAbsolutePath()+File.separator+task);
        if(!f.exists()){
            System.out.print("No such file exists: "+f.getAbsolutePath()+"\n");
            return;
        }
        byte[] ba = new byte[(int) f.length()];  
        FileInputStream fis = new FileInputStream(f);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(ba,0,ba.length);
        
        Task t = new Task(task, typ, ba.length, ba);
        //TODO send this to manager
        
    }
    
    
    /*
     * Preconditions: logged in, taskEngine assigned to task, task file still exists
     * Postconditions: task starts executing
     */
    private void executeTask(int id, String script){
        //TODO remote
        
        /*Socket tsock = null;
        PrintWriter tout = null;
        DataOutputStream dout = null;
        BufferedReader tin = null;
        
        Task t = getTask(id);
        if(t == null){
            System.out.print("No Task with id: "+id+" prepared.\n");
            return;
        }
        if(t.status != TASKSTATE.assigned){
            System.out.print("Status of task is "+t.status.toString()+" but must be assigned for execute to work.\n");
            return;
        }
        
        try {
            tsock = new Socket(t.taskEngine,t.port);
            dout = new DataOutputStream(tsock.getOutputStream());
            tout = new PrintWriter(tsock.getOutputStream());
        } catch (UnknownHostException e) {
            System.out.print("The Host Task Engine "+t.taskEngine+" is unknown. Can not connect.\n");
            if(DEBUG){e.printStackTrace();}
            return;
        } catch (IOException e) {
            System.out.print("Sorry encounterd a problem in opening the outgoing Task Engine socket.\n");
            if(DEBUG){e.printStackTrace();}
        }
        
        try {
            tin = new BufferedReader(new InputStreamReader(tsock.getInputStream()));
        } catch (IOException e) {
            System.out.print("Could not listen for replay from Task Engine.\n");
            if(DEBUG){e.printStackTrace();}
            return;
        }
    
        File f = new File(tdir.getAbsolutePath()+File.separator+t.name);
        //Transmit the command string string.
        //BEWARE THIS IS UNVALIDATE USER INPUT!!!        
        tout.println(script);
        tout.println(id);
        tout.println(t.name);
        tout.println(t.type.toString());
        tout.println(f.length());
        tout.flush();
        try {
            tin.readLine(); //this is for sync; a Send will be received maybe useful for later implementations
        } catch (IOException e1) {
            if(DEBUG){e1.printStackTrace();}
        }
        
        
        

        Listener L = new Listener(tsock, tin);
        L.start();

        
        byte[] ba = new byte[(int) f.length()];  //this is not great but it works
        try {
            FileInputStream fis = new FileInputStream(f);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(ba,0,ba.length);
            dout.write(ba,0,ba.length);
            dout.flush();
        } catch (FileNotFoundException e) {
            System.out.print("Sorry but "+t.name+" seems to be inexistant.\n");
            if(DEBUG){e.printStackTrace();}
            return;
        } catch (IOException e) {
            System.out.print("There was a problem with the remote connection, could not send file.\n");
            if(DEBUG){e.printStackTrace();}
            return;
        }
        System.out.print("Transmitted Task!\n");
        t.status = TASKSTATE.executing;*/

    }
    
    
    // Local functions
    
    
    /*
     * Preconditions: none
     * Postconditions: printed all files in task directory to stdout
     */
    private void list(){
        String cont[] = tdir.list();
        int i = 0;
        while(i < cont.length){
            System.out.print(cont[i]+"\n");
            i++;
       }
    }

    /*
     * Preconditions: none
     * Postconditions: Task info printed to std out
     */
    private void info(int id){
    //TODO fit to remote
        
    }
    
    /*
     * Preconditions: none
     * Postconditions: all open handles are released, program terminates
     */
    private void exit(){
        System.out.print("Exiting on request. Good Bye!\n");
        logout();
        e.shutdownNow();
        closeSchedulerConnection();
    }
    
    
    // Assistance functions
    
    
    /*
     * Preconditions: Logged in to Scheduler
     * Postconditions: Scheduler connection terminated, variables set to null
     */
    private void closeSchedulerConnection(){
        try{
            if(sin != null){sin.close();}
            if(sin != null){sout.close();}
            if(sin != null){ssock.close();}
        }
        catch (Exception e){
            if(DEBUG){e.printStackTrace();}
            return;
        }
        sin = null;
        sout = null;
        ssock = null;
        return;
    }
  
    
    
    //  Nested Classes and Main
    
    
    private class Listener extends Thread{
        
        private Socket lsock;
        private BufferedReader lin;
        
        public Listener(Socket s, BufferedReader i){
            lsock = s;
            lin = i;
        }
        
        /*
         * Preconditions: sock,in not null 
         * Postconditions: continuous listening for messages from server
         */       
        public void run(){
            String rcv = "nothing recieved\n";
            while(lsock.isConnected()){
                try {
                    rcv = lin.readLine();
                } catch (IOException e) {
                    System.out.print("Could not read from socket.\n");
                    if(DEBUG){e.printStackTrace();}
                    Listener.this.exit();
                }
                try{
                if(rcv.contentEquals("Successfully logged out.") || rcv.contains("Wrong company or password.")){
                    System.out.print(rcv +"\n");
                    return;
                }
                if(rcv.contains("Not enough capacity. Try again later.")){
                    System.out.print("Not enough capacity. Try again later.\n");
                    rcv = "";
                }
                if(!rcv.contentEquals("")){
                    System.out.print(rcv +"\n");
                }
                }
                catch(NullPointerException e){
                    if(DEBUG){e.printStackTrace();}
                    System.out.println("Server hung up. Seems he went down hard.");
                    return;
                }
            }
            return;

        }
        
        private void exit(){
            try {
                lin.close();
                lsock.close();
            } catch (IOException e) {
                if(DEBUG){e.printStackTrace();}
            }
            closeSchedulerConnection();
            exit();
            
        }
        
        /*
         * Preconditions: thread is running
         * Postconditions: thread terminated
         */
        public void interrupt(){
            try{
                lsock.close();
                lin.close();
            }
            catch(Exception e){
                //do nothing about it you are going down forcefully
            }
            return;
        }
    }
    

    
    public static void main (String args[]){
        
        final String usage = "DSLab Client usage: java Client.java schedulerHost schedulerTCPPort taskdir";
        Client c;
        
        if(args.length != 3){
            System.out.print(usage);
            System.exit(1); //return value
        }
        
        try{
            c = new Client(args[0], Integer.parseInt(args[1]), args[2]);
            c.startUp();
            c.run();
        } catch(NumberFormatException e){
            System.out.print("Second argument must be an Integer value.\n");
            System.exit(1);//return value
        } catch (IOException e) {
            if(DEBUG){e.printStackTrace();}
        }
        
    }
}

