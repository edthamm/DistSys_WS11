
public class User {
        protected String name = "";
        String password = ""; //this is inherently unsafe in production use encryption
        volatile int low = 0;
        volatile int middle = 0;
        volatile int high = 0;
        private int credits = 0;
        protected Callbackable callback = null;
        
        public User(String n, String pw, int c){
            name = n;
            password = pw;
            setCredits(c);
        }
        public User(String n, String pw){
            name = n;
            password = pw;
        }
        
        public String toString(){
            if(callback != null){
                return(name+" (online) LOW: "+low+", MIDDLE: "+middle+", HIGH: "+high+"\n");
            }
            return(name+" (offline) LOW: "+low+", MIDDLE: "+middle+", HIGH: "+high+"\n");
        }
        
        public boolean verify(String pw){
            if(pw.contentEquals(password)&&callback == null){//only one comapny at a time
                return true;
            }
            return false;
        }
        
        public int totalTasks(){
            return high+middle+low;
        }
        
        public synchronized void setCredits(int credits) {
            this.credits = credits;
        }
        public synchronized int getCredits() {
            return credits;
        }
}
