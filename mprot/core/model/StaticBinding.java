package mprot.core.model;
//represent a static binding, both ways
//such as <node, requirement> and <node, capability>
public class StaticBinding {
    private final String node;
    private final String capOrReq;

    public StaticBinding(String node, String capOrReq) 
        throws 
            NullPointerException, 
            IllegalArgumentException
    {
        if(node == null)
            throw new NullPointerException("node null");
        if(capOrReq == null)
            throw new NullPointerException("capOrReq null");
       
        if(node.isEmpty() == true)
            throw new IllegalArgumentException("node empty"); 
        if(capOrReq.isEmpty() == true)
            throw new IllegalArgumentException("capOrReq empty");
        
        this.capOrReq = capOrReq;
        this.node = node;
    }

    public String getCapOrReq() {
        return this.capOrReq;
    }

    public String getNodeName() {
        return this.node;
    }

    @Override
    public boolean equals(Object sb){
        boolean ret = false;
        StaticBinding toCheck = (StaticBinding) sb;
        if((toCheck.capOrReq.equals(this.capOrReq) == true) && (toCheck.node.equals(this.node) == true))
            ret = true;

        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.capOrReq.hashCode();
        result = 31 * result + this.node.hashCode();
        return result;
    }

    
}