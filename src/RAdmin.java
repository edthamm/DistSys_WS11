import java.rmi.RemoteException;
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

        public Set<Entry<Integer, Double>> getPrices() throws RemoteException {
            return Prices.entrySet();
        }

        public void logout() throws RemoteException {
            User me = Users.get(name);
            me.callback.sendMessage("Thanks for using our services. Bye.");
            me.callback = null;
            
        }

        public void setPrice(int step, double discount) throws RemoteException {
            if(Prices.containsKey(Integer.valueOf(step))){
                Prices.replace(Integer.valueOf(step), Double.valueOf(discount));
            }
            else{
                Prices.put(Integer.valueOf(step), Double.valueOf(discount));
            }
            return;           
        }
        
    }