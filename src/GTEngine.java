

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
/**
 * 
 * @author Eduard Thamm 0525087
 *
 */
public class GTEngine extends AbstractServer {
    
    private String schedIP;
    private int schedUPort;
    private File tdir;
    private int isAl;
    private int minC;
    private int maxC;
    private int load = 0;
    private final static String usage = "Usage GTEngine tcpPort schedulerHost schedulerUDPPort alivePeriod minComsumption maxConsumption taskDir";
    private final static boolean DEBUG = true;
    
    public GTEngine(int tp, String sched, int udp, int ia, int min, int max, String td){
        Tport = tp;
        schedIP = sched;
        schedUPort = udp;
        isAl = ia;
        minC = min;
        maxC = max;
        tdir = new File(td);
        if (!tdir.exists()){
            System.out.print(tdir.getName()+"does not exists.\n");
            System.exit(1);
        }
        if(!tdir.isDirectory()){
            System.out.print(tdir.getName()+"is not a directory.\n");
            System.exit(1);
        }        
    }
    
    private void UDPListen(){
        
    }
    
    private void startIsAlive(){
        
    }
    
    
    
    
    public static void main(String[] args) {
        if(args.length != 7){
            System.out.println(usage);
            System.exit(1);
        }
        
        try{
            GTEngine gt = new GTEngine(Integer.parseInt(args[0]),args[1],Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]),Integer.parseInt(args[5]),args[6]);
            gt.inputListen();
            gt.UDPListen();
            gt.tcpListen();
            gt.startIsAlive();
            
            
        }
        catch(NumberFormatException e){
            if(DEBUG){e.printStackTrace();}
            System.out.println("All input numbers must be Integers.");
            System.exit(1);
        } catch (IOException e) {
            
            if(DEBUG){e.printStackTrace();}
        }
        
    }
    
    
    @SuppressWarnings("unused")//called by superclass
    private class Worker extends AbstractServer.Worker{
        
        
        public Worker(Socket s) {
            super(s);
        }
        
        public void run(){
            String execln;
            String tid;
            String tname;
            long flength;
            
            try {
                BufferedReader textin = new BufferedReader(new InputStreamReader(Csock.getInputStream()));
                DataInputStream datain= new DataInputStream(Csock.getInputStream());
                
                execln = textin.readLine();
                tid = textin.readLine();
                tname = textin.readLine();
                flength = Long.parseLong(textin.readLine());
                
                int num = 1;
                while(new File(tdir.getAbsolutePath()+tname+num).exists()){
                    num++;
                }
                File f = new File(tdir.getAbsolutePath()+tname+num);
                f.createNewFile();
                
                byte[] ba = new byte[(int) flength];
                FileOutputStream fos = new FileOutputStream(f);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                datain.read(ba, 0, ba.length);
                
                bos.write(ba);
                bos.flush();
                bos.close();
                fos.close();
                
                //TODO rewrite execln and then fork
                
                
            } catch (IOException e) {
                System.out.println("Could not read from Socket");
                if(DEBUG){e.printStackTrace();}
                return;
            }
        }
    }

    @SuppressWarnings("unused")//called in superclass
    private class InputListener extends AbstractServer.InputListener{
        public void run(){
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String userin;
            
            try{
                while((userin = stdin.readLine()) != null){
                    if(userin.contentEquals("!load")){
                        System.out.println("Current load: " + load);
                        userin = "";
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
    

}
