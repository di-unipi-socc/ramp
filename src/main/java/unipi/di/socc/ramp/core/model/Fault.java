package unipi.di.socc.ramp.core.model;

import java.util.Objects;

/**
 * represent a fault: requirement needed but not satisfied
    * fault f = <ID of the instance, faulted requirement> 
 */

public class Fault {
    private final String instanceID;
    private final Requirement req;

    /**
     * @param instanceID identifier of the instance with a faulted requirement
     * @param req faulted requirement
     */
    public Fault(String instanceID, Requirement req) {
        if(instanceID == null || req == null)
            throw new NullPointerException();
        
        if(instanceID.isBlank())
            throw new IllegalArgumentException();

        this.instanceID = instanceID;
        this.req = req;
    }

    public String getNodeInstanceID() {
        return instanceID;
    }
    public Requirement getReq() {
        return req;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.req, this.instanceID);
    }
    @Override
    public boolean equals(Object obj){
        if(obj == null)
            throw new NullPointerException();

        Fault f = (Fault) obj;
        return this.instanceID.equals(f.getNodeInstanceID()) && this.req.equals(f.getReq());
    }
}
