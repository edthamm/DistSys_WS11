
public class Task {
    public TASKTYPE ttype;
    public String tname;
    public int flength;
    public byte[] binary;


    
    public Task(String name, TASKTYPE type, int length, byte[] bin){
        tname = name;
        ttype = type;
        flength = length;
        binary = bin;
    }
}
