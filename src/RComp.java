import java.io.BufferedReader;
import java.io.PrintWriter;
import java.rmi.RemoteException;
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
            Users.get(name).credits += amount;
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
                TaskEServ.execute(new TaskExecutor(t, cb, Users, Prices, schedin, schedout));
            }
                return;
        }

        public int getCredits() throws RemoteException {
            return Users.get(name).credits;
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
    }
    

