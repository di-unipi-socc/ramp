package unipi.di.socc.ramp.core.analyzer.actions;

import java.util.Objects;

public class OpStart extends Action{

    private final String instanceID;
    private final String opName;

    public OpStart(String instanceID, String opName){
        this.setAction();
        this.instanceID = instanceID;
        this.opName = opName;
    }

    public void setAction(){
        this.action = "opStart";
    }

    public String getOpName() {
        return opName;
    }

    public String getInstanceID() {
        return instanceID;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof OpStart){
            OpStart check = (OpStart) obj;
            return 
                check.getInstanceID().equals(this.instanceID) && 
                check.getOpName().equals(this.opName);
        }

        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.action, this.instanceID, this.opName);
    }

}
