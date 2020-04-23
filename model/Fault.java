package model;

//this is a couple (tuple). we do not use Map.Entry for readibility.
//this is a fault such as <instanceID, failed requirement>
public class Fault {
    private final String instanceID;
    private final Requirement req;

    public Fault(String instanceID, Requirement req) {
        assert instanceID != null;
        assert req != null;

        this.instanceID = instanceID;
        this.req = req;
    }

    public String getInstanceID() {
        return this.instanceID;
    }

    public Requirement getReq() {
        return this.req;
    }

    @Override
    /**
     * two fault are equal when they are about the same insance and requirement
     */
    public boolean equals(Object f){
        Fault toCheck = (Fault) f;
        boolean ret = false;

        if(toCheck.instanceID.equals(this.instanceID) && toCheck.req.getName().equals(this.req.getName()))
            ret = true;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.instanceID.hashCode();
        result = 31 * result + this.req.getName().hashCode();
        return result;
    }
}