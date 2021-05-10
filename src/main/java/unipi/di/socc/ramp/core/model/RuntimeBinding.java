package unipi.di.socc.ramp.core.model;

import java.util.Objects;

/**
 * models a binding of an instance in the global state
    * instance X with requirements r1, r2, ..., rn
    * X will have a list of RuntimeBinding such as
        * <satisfied requirement, id of the instance which is satisfying the requirement>
 */

public class RuntimeBinding {
    
    private final Requirement req;
    private final String instanceID;

    /**
     * @param req requirement which is being satisfied
     * @param instanceID identifier of the instance that is satisfying the requirement req
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public RuntimeBinding(Requirement req, String instanceID) 
        throws
            NullPointerException,
            IllegalArgumentException
    {
        if(req == null || instanceID == null)
            throw new NullPointerException();

        if(instanceID.isBlank())
            throw new IllegalArgumentException();

        this.req = req;
        this.instanceID = instanceID;
    }

    public Requirement getReq() {
        return req;
    }
    public String getNodeInstanceID() {
        return instanceID;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.req, this.instanceID);
    }
    @Override
    public boolean equals(Object obj){
        if(obj == null)
            throw new NullPointerException();
            
        RuntimeBinding runBinding = (RuntimeBinding) obj;
        return this.instanceID.equals(runBinding.getNodeInstanceID()) && this.req.equals(runBinding.getReq());
    }


}
