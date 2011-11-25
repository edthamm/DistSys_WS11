import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


    public class RAdmin implements Adminable{
        private String name ="";
        private ConcurrentHashMap<String,User> Users = new ConcurrentHashMap<String,User>();
        private ConcurrentHashMap<Integer,Double> Prices = new ConcurrentHashMap<Integer,Double>();
        
        public RAdmin(String n,ConcurrentHashMap<String,User> u,ConcurrentHashMap<Integer,Double>p){
            name = n;
            Users = u;
            Prices = p;
        }

        public void getPrices() throws RemoteException {
            Callbackable cb = Users.get(name).callback;
            Set<Entry<Integer, Double>> es = Prices.entrySet();
            Iterator<Entry<Integer, Double>> i = es.iterator();
            cb.sendMessage("Tasks=Percent");
            while(i.hasNext()){
                cb.sendMessage(i.next().toString());
            }
            return;
        }

        public void logout() throws RemoteException {
            User me = Users.get(name);
            me.callback.sendMessage("Thanks for using our services. Bye.");
            me.callback = null;
            UnicastRemoteObject.unexportObject(this, true);
            
        }

        public void setPrice(int step, double discount) throws RemoteException {
            
            //TODO values in range? step >0; 0<= discount <= 100
            if(Prices.containsKey(Integer.valueOf(step))){
                Prices.replace(Integer.valueOf(step), Double.valueOf(discount));
            }
            else{
                Prices.put(Integer.valueOf(step), Double.valueOf(discount));
            }
            return;           
        }
        
    }