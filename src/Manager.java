import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;



public class Manager {
    private static final boolean DEBUG = true;
    private static final String usage = "Usage: bindingName schedulerHost schedulerTCPPort";
    private String bindingName;
    private String schedHost;
    private int schedTP;
    private ConcurrentHashMap<String,User> Users = new ConcurrentHashMap<String,User>();

    
    public Manager(String bn, String sh, int tp){
        bindingName = bn;
        schedHost = sh;
        schedTP = tp;
    }
    
    public void inputListen(){
        InputListener i = new InputListener();
        i.start();        
    }
    
    private void exitRoutine(){
        //TODO
    }
    
    private void exitRoutineFail(){
        //TODO
    }
    
    private void readProperties() throws FileNotFoundException{
        readRegistry();
        readUsers();
    }
    
    private void readRegistry() throws FileNotFoundException{
        InputStream in = null;
        in = ClassLoader.getSystemResourceAsStream("company.properties");
        if(in != null){
            java.util.Properties registry = new java.util.Properties();
            try {
                registry.load(in);
                Set<String> registryprops = registry.stringPropertyNames();
                
                for (String prop : registryprops){
                    String p = registry.getProperty(prop);
                    if(prop.contentEquals("registry.host")){
                        //TODO
                    }
                    if(prop.contentEquals("registry.port")){
                        //TODO
                    }
                }
                
            } catch (IOException e) {
                System.out.print("Could not read from registry.properties. Exiting.\n");
                exitRoutineFail();
                if(DEBUG){e.printStackTrace();}
            }
        }
        else{
            throw new FileNotFoundException();
        }
    }
    
    private void readUsers() throws FileNotFoundException{
        InputStream in = null;
        in = ClassLoader.getSystemResourceAsStream("user.properties");
        if(in != null){
            java.util.Properties users = new java.util.Properties();
            try {
                users.load(in);
                Set<String> userNames = users.stringPropertyNames();
                String name = "";
                String pw = "";
                
                for (String userName : userNames){
                    String attribute = users.getProperty(userName);
                    String user[] = userName.split(".");
                    if(user.length == 1){
                        name = userName;
                        pw = attribute;
                    }
                    else{
                        if(user[1].contentEquals("admin")){
                            if(attribute.contentEquals("true")){
                                Users.put(name, new Admin(name, pw));
                            }
                        }
                        if(user[1].contentEquals("credits")){
                            Users.put(name, new User(name, pw, Integer.parseInt(attribute)));
                        }
                    }
                }
                
            } catch (IOException e) {
                System.out.print("Could not read from user.properties. Exiting.\n");
                exitRoutineFail();
                if(DEBUG){e.printStackTrace();}
            }
            catch(NumberFormatException e){
                System.out.print("Your user.proerties file is malformed, A credit value contains a non integer value.\n");
                if(DEBUG){e.printStackTrace();}
            }
        }
        else{
            throw new FileNotFoundException();
        }
    }

    public static void main(String[] args) {
        if(args.length != 3){
            System.out.print(usage);
            System.exit(1);
        }
        
        try {
            Manager m = new Manager(args[0], args[1], Integer.parseInt(args[2]));
            
            m.readProperties();
            
            
        } catch (IOException e) {
            System.out.println("Ran in to an I/O Problem. Most likely Some config file is missing.");
            if(DEBUG){e.printStackTrace();}
            return;
        }
          catch(NumberFormatException e){
            System.out.print(usage + "All number values must be integers.\n");
            if(DEBUG){e.printStackTrace();}
            System.exit(1);
        }
    }
    
    
    private class InputListener extends Thread{
        public void run(){
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String userin;
            
            try {
                while((userin = stdin.readLine()) != null){
                    if(userin.contentEquals("!users")){
                       
                    }
                    if(userin.contentEquals("!exit")){
                        System.out.println("Exiting on request. Bye.");
                        exitRoutine();
                        return;
                    }
                    if(!userin.contentEquals("")){
                        System.out.print("Unknown command.\n");
                    }
                }
            } catch (IOException e) {
                System.out.print("Could not read from stdin.\n");
                if(DEBUG){e.printStackTrace();}
                return;
            }
        }
        
    }
    
    private class User{
        private String name = "";
        private String password = ""; //this is inherently unsafe in production use encryption
        private COMPANYCONNECT line = COMPANYCONNECT.offline;
        private int low = 0;
        private int middle = 0;
        private int high = 0;
        private int credits = 0;
        
        public User(String n, String pw, int c){
            name = n;
            password = pw;
            credits = c;
        }
        public User(String n, String pw){
            name = n;
            password = pw;
        }
        
        public String toString(){
            return(name+" ("+line.toString()+") LOW: "+low+", MIDDLE: "+middle+", HIGH: "+high+"\n");
        }
        
        public boolean verify(String pw){
            if(pw.contentEquals(password)){
                return true;
            }
            return false;
        }
    }
    private class Admin extends User{
        
        public Admin(String n, String pw){
            super(n,pw);
        }
        
        public String toString(){
            return(super.name+" ("+super.line.toString()+")");
        }
        

    }

}
