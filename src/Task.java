
public class Task {
    public String execln;
    public String tid;
    public String ttype;
    public String tname;
    public long flength;
    public byte[] binary;
    
    public Task(String name, String type, String execln, long length, byte[] bin){
        tname = name;
        ttype = type;
        this.execln = execln;
        flength = length;
        binary = bin;
    }
}
