
public class User {
        protected String name = "";
        private String password = ""; //this is inherently unsafe in production use encryption
        int low = 0;
        int middle = 0;
        int high = 0;
        volatile int credits = 0;
        protected Callbackable callback = null;
        
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
}
