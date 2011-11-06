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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.*;

public class Client implements Callbackable{
    
    private int port;
    private String server;
    private Socket ssock;
    private PrintWriter sout;
    private BufferedReader sin;
    private File tdir;
    private ExecutorService e = Executors.newCachedThreadPool();
    private static final boolean DEBUG = false;
    private Remote cb;

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
    
    public void createStub(){
        try {
            cb = UnicastRemoteObject.exportObject(this, 0);
        } catch (RemoteException e) {
            if(DEBUG){e.printStackTrace();}
            System.out.println("Could not export Callback. Exiting...");
            exit();
            return;
        }
    }
    
    public void sendMessage(String msg){
        System.out.println(msg);
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
        //TODO remote
    }
    
    /*
     * Preconditions: logged in
     * Postconditions: logged out
     */
    private void logout(){
        //TODO remote
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

