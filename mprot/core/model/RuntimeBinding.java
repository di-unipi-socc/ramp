package mprot.core.model;

/**
 * this is just a pair (tuple). we do not use Map.Entry for readibility
 * this represent a binding, given the requirement r, if the node instanceIDance n 
 * is the one who satisfy r offering the right capability, we have a bond, 
 * such as <r, unique id of n> in the global state (binding)
 */
public class RuntimeBinding {
    private final Requirement requirement;
    private final String targetInstanceID;

    /**
     * @param requirement requirement needed by some node instanceIDance 
     * @param targetInstanceID node instance that take care of requirement
     * @throws NullPointerException
     */
    public RuntimeBinding(Requirement requirement, String targetInstanceID) {
        
        if(targetInstanceID == null)
            throw new NullPointerException("targetInstanceID null");
        if(targetInstanceID.isEmpty() == true)
            throw new IllegalArgumentException("targetInstanceID empty");
        if(requirement == null)
            throw new NullPointerException("requirement null");
        
        this.targetInstanceID = targetInstanceID;
        this.requirement = requirement;
    }

    /**
     * @return node instanceIDance that take care of the requirement
     */
    public String getNodeInstanceID() {
        return targetInstanceID;
    }

    /**
     * @return requirement needed by some node instanceIDance
     */
    public Requirement getReq() {
        return requirement;
    }

    @Override
    /**
     */
    public boolean equals(Object f){
        RuntimeBinding toCheck = (RuntimeBinding) f;
        boolean ret = false;

        if(toCheck.getNodeInstanceID().equals(this.targetInstanceID) && toCheck.getReq().equals(this.requirement))
            ret = true;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.targetInstanceID.hashCode();
        result = 31 * result + this.requirement.hashCode();
        return result;
    }
}