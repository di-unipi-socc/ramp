package mprot.core.model;

/**
 * this is just a pair (tuple). we do not use Map.Entry for readibility
 * this represent a binding, given the requirement r, if the node instanceIDance n 
 * is the one who satisfy r offering the right capability, we have a bond, 
 * such as <r, unique id of n> in the global state (binding)
 */
public class RuntimeBinding {
    private final String instanceID;
    private final Requirement req;

    /**
     * @param req requirement needed by some node instanceIDance 
     * @param instanceID node instance that take care of req
     * @throws NullPointerException
     */
    public RuntimeBinding(Requirement req, String instanceID) {
        
        if(instanceID == null)
            throw new NullPointerException("instanceID null");
        if(instanceID.isEmpty() == true)
            throw new IllegalArgumentException("instanceID empty");
        if(req == null)
            throw new NullPointerException("req null");
        
        this.instanceID = instanceID;
        this.req = req;
    }

    /**
     * @return node instanceIDance that take care of the requirement
     */
    public String getNodeInstanceID() {
        return instanceID;
    }

    /**
     * @return requirement needed by some node instanceIDance
     */
    public Requirement getReq() {
        return req;
    }

    @Override
    /**
     */
    public boolean equals(Object f){
        RuntimeBinding toCheck = (RuntimeBinding) f;
        boolean ret = false;

        if(toCheck.getNodeInstanceID().equals(this.instanceID) && toCheck.getReq().equals(this.req))
            ret = true;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.instanceID.hashCode();
        result = 31 * result + this.req.hashCode();
        return result;
    }
}