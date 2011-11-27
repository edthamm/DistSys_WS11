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
        User me;
        
        public RAdmin(String n,ConcurrentHashMap<String,User> u,ConcurrentHashMap<Integer,Double>p){
            name = n;
            Users = u;
            Prices = p;
            me = Users.get(name);
        }

        public void getPrices() throws RemoteException {
            Callbackable cb = me.callback;
            Set<Entry<Integer, Double>> es = Prices.entrySet();
            Iterator<Entry<Integer, Double>> i = es.iterator();
            cb.sendMessage("Tasks=Percent");
            while(i.hasNext()){
                cb.sendMessage(i.next().toString());
            }
            return;
        }

        public void logout() throws RemoteException {
            me.callback.sendMessage("Thanks for using our services. Bye.");
            me.callback = null;
            UnicastRemoteObject.unexportObject(this, true);
            
        }

        public void setPrice(int step, double discount) throws RemoteException {           
            if (step >= 0 && 0 <= discount && discount <= 100) {
                if (Prices.containsKey(Integer.valueOf(step))) {
                    Prices.replace(Integer.valueOf(step), Double
                            .valueOf(discount));
                } else {
                    Prices.put(Integer.valueOf(step), Double.valueOf(discount));
                }
                me.callback.sendMessage("Discount from " + step
                        + " tasks onward set to: " + discount);
                return;
            }
            else{
                me.callback.sendMessage("Sorry number of tasks must be positive and discount between 0 and 100 percent.");
                throw new RemoteException("Sorry number of tasks must be positive and discount between 0 and 100 percent.");
            }
        }
        
    }