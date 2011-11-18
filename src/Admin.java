public class Admin extends User{
        
        public Admin(String n, String pw){
            super(n,pw);
        }
        
        public String toString(){
            if(super.callback != null){
                return(super.name+" (online)");
            }
            return(super.name+" (offline)");
        }
        

    }

