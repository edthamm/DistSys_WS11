import java.io.Serializable;


public class Task implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = -3119563640144072803L;
    /**
     * 
     */
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
