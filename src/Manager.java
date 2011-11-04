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

    
    public Manager(String bn, String sh, int tp){
        bindingName = bn;
        schedHost = sh;
        schedTP = tp;
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
                
                for (String userName : userNames){
                    String attribute = users.getProperty(userName);
                    //TODO initialize objects
                }
                
            } catch (IOException e) {
                System.out.print("Could not read from user.properties. Exiting.\n");
                exitRoutineFail();
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

}
