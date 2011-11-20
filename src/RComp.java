import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RComp implements Companyable{
        private String name ="";
        private Callbackable cb = null;
        private ConcurrentHashMap<String,User> Users;
        private ConcurrentHashMap<Integer,MTask> Tasks;
        private ExecutorService TaskEServ = Executors.newCachedThreadPool();
        private ConcurrentHashMap<Integer,Double> Prices;
        private BufferedReader schedin;
        private PrintWriter schedout;
        private static final boolean DEBUG = true;
        
        public RComp(String n, ConcurrentHashMap<String,User> u, ConcurrentHashMap<Integer,MTask> t,ConcurrentHashMap<Integer,Double> p,BufferedReader i,PrintWriter o){
            name = n;
            Users = u;
            Tasks = t;
            cb = Users.get(name).callback;
            Prices = p;
            schedin = i;
            schedout = o;
        }

        public boolean buyCredits(int amount) throws RemoteException {
            Users.get(name).setCredits(Users.get(name).getCredits() + amount);
            return true;
        }

        public void executeTask(int id, String execln) throws RemoteException {
            MTask t = Tasks.get(Integer.valueOf(id));
            if(t == null){
                cb.sendMessage("No Task with id: "+id+" known.");
                return;
            }
            if(t.owner.contentEquals(name)){
                t.execln = execln;
                TaskEServ.execute(new TaskExecutor(t, cb));
            }
                return;
        }

        public int getCredits() throws RemoteException {
            return Users.get(name).getCredits();
        }

        public void getOutputOf(int id) throws RemoteException {
            if(Tasks.containsKey(id)){
                MTask T = Tasks.get(id);
                if(T.owner.contentEquals(name)){
                    cb.sendMessage(T.output);
                    return;
                }
                else{
                    cb.sendMessage("This Task does not belong to you!");
                    return;
                }
            }
            cb.sendMessage("Sorry. Task inexistant.");
            
        }

        public void getTaskInfo(int id) throws RemoteException {
            if(Tasks.containsKey(id)){
                MTask t = Tasks.get(id);
                if(t.owner.contentEquals(name)){
                    cb.sendMessage("Task: "+id+" ("+t.tname+")\n"+
                                   "Type: "+t.ttype.toString()+"\n"+
                                   "Assigned Engine: "+t.taskEngine+":"+t.port+"\n"+
                                   "Status: "+t.status.toString()+"\n"+
                                   "Costs: "+t.cost);
                    return;
                }
                else{
                    cb.sendMessage("This Task does not belong to you!");
                    return;
                }
            }
            cb.sendMessage("Sorry. Task inexistant.");
            
            
        }

        public void logout() throws RemoteException {
            Callbackable c = cb;
            c.sendMessage("Logging out...");
            Users.get(name).callback = null;
            c.sendMessage("done");
            
        }

        public boolean prepareTask(Task t) throws RemoteException {
            MTask mt = new MTask(t);
            mt.status = TASKSTATE.prepared;
            Tasks.put(Integer.valueOf(mt.id), mt);
            return true;
        }
        

        private class TaskExecutor implements Runnable{
                MTask m;
                Callbackable cb;
                
                public TaskExecutor(MTask mt, Callbackable c){
                    m = mt;
                    cb = c;

                }

                public void run() {
                    try{
                    
                    schedout.println("!requestEngine "+m.id+" "+m.ttype.toString());
                    
                    String rcv = schedin.readLine();//TODO will i get the next or the one concerning me?
                    //TODO assign fail and win
                    }
                    catch(IOException e){
                        if(DEBUG){e.printStackTrace();}
                    }
                    
                    
                    //Start talking to GTE
                    Socket tsock = null;
                    PrintWriter tout = null;
                    DataOutputStream dout = null;
                    BufferedReader tin = null;
                    
                    
                    try {
                        tsock = new Socket(m.taskEngine,m.port);
                        dout = new DataOutputStream(tsock.getOutputStream());
                        tout = new PrintWriter(tsock.getOutputStream());
                    } catch (UnknownHostException e) {
                        System.out.print("The Host Task Engine "+m.taskEngine+" is unknown. Can not connect.\n");
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
                
                    //Transmit the command string string.
                    //BEWARE THIS IS UNVALIDATE USER INPUT!!!        
                    tout.println(m.execln);
                    tout.println(m.id);
                    tout.println(m.tname);
                    tout.println(m.ttype.toString());
                    tout.println(m.flength);
                    tout.flush();
                    try {
                        tin.readLine(); //this is for sync; a Send will be received maybe useful for later implementations
                    } catch (IOException e1) {
                        if(DEBUG){e1.printStackTrace();}
                    }
                    

                    try {
                        byte[] ba = m.binary;
                        dout.write(ba,0,ba.length);
                        dout.flush();
                    }
                        catch (IOException e) {
                        System.out.print("There was a problem with the remote connection, could not send file.\n");
                        if(DEBUG){e.printStackTrace();}
                        return;
                    }
                        
                        
                    try {
                        String in;
                        if((in = tin.readLine()).contains("execution started")){
                            m.status = TASKSTATE.executing;
                            m.start = System.currentTimeMillis();
                        }
                        else{
                            m.status = TASKSTATE.prepared;
                            cb.sendMessage("Currently no engine available, try again later.");
                            return;
                        }

                        while((in = tin.readLine())!= null){
                            m.output.concat(in);
                        }
                        m.status = TASKSTATE.finished;
                        m.finish = System.currentTimeMillis();
                        cb.sendMessage("Execution of Task "+m.id+" finished.");
                        
                        long time = m.finish-m.start;
                        int cost = calcCost(time);
                        Users.get(m.owner).setCredits(Users.get(m.owner).getCredits() - cost);
                        
                        tout.close();
                        dout.close();
                        tin.close();
                        tsock.close();
                        return;
                        
                    } catch (IOException e) {
                        if(DEBUG){e.printStackTrace();}
                    }

                }

                private int calcCost(long time) {
                    User u = Users.get(m.owner);
                    int total = u.high+u.middle+u.low;
                    Integer max = 0;
                    int price;
                    double discount = 0;
                    Enumeration<Integer> k = Prices.keys();
                    while (k.hasMoreElements()){
                        Integer s = k.nextElement();
                        int t = s.intValue();
                        if(total > t && s > max){ //falls die anzahl der gesamtauftr�ge h�her ist als diese stufe, und keine h�here stufe vorher angetroffen wurde
                            discount = Prices.get(s);                   
                        }
                    }
                    
                    discount= discount/100;
                    price = Double.valueOf(((10*time)-((10*time)*discount))).intValue();//TODO check espec for minute
                    
                    return price;
                }
                
                
                
        }

    }
    

