package unipi.di.socc.ramp.core.analyzer.actions;

import java.util.Objects;

public class ScaleIn extends Action{

    private final String instanceID;

    public ScaleIn(String instanceID){
        this.setAction();
        this.instanceID = instanceID;
    }

    public void setAction(){
        this.action = "scaleIn";
    }

    public String getInstanceID() {
        return instanceID;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof ScaleIn){
            ScaleIn check = (ScaleIn) obj;
            return check.getInstanceID().equals(this.instanceID);
        }

        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.action, this.instanceID);
    }
    
}
